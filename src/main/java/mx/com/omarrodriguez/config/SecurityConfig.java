/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.omarrodriguez.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


/**
 *
 * @author omar
 */
@Configuration
@EnableWebSecurity(debug = false)
public class SecurityConfig extends WebSecurityConfigurerAdapter{
    
    /**
     * Servicio personalizado para buscar usuarios, la interfaz UserDetalService
     * requiere la implementacion de un metodo loadUserByUsername, de esta forma
     * delegamos la busqueda del usuario a un servicio (puede ser archivos o base
     * de datos)
     */
    private UserDetailsService userDetailsService;

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    
    /**
     * En realidad lo importante de la siguiente llamada es el anotar el metodo
     * como Bean, de esta manera podemos usar el metodo como una fabrica de
     * AuthenticationManager
     * @return
     * @throws Exception 
     */
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean()
            throws Exception {
        return super.authenticationManagerBean();
    }

   
    /**
     * Sobre escritura del bean de cors filter, sin este metodo no se pueden
     * ejecutar todos los vervos de HTTP
     * @return 
     */
    @Bean
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
        config.addAllowedMethod(HttpMethod.POST);
        config.addAllowedMethod(HttpMethod.GET);
        config.addAllowedMethod(HttpMethod.PUT);
        config.addAllowedMethod(HttpMethod.DELETE);
        config.addAllowedMethod(HttpMethod.OPTIONS);
        config.addAllowedMethod(HttpMethod.HEAD);
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    /**
     * Mediante la sobrecarga de los metodos de config hacemos el trabajo pesado
     * de configuracion, en el siguiente caso se configura el Auth Manager para
     *    1) usar el servicio de detalles de usuario requerido
     *    2) fijar el password encoder
     * @param auth
     * @throws Exception 
     */
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
          .userDetailsService(userDetailsService)
          .passwordEncoder(new BCryptPasswordEncoder());
    }
    
    /**
     * Se configuran los endpoints, se indican que ciertos patrones van 
     * sin autenticacion (/h2*) y se indica que se va a proporcionar un formulario
     * de login
     * @param http
     * @throws Exception 
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          .authorizeRequests()
          .antMatchers("/h2*").anonymous()               
          .anyRequest().authenticated()
        .and()
          .formLogin().permitAll()
        .and()
          .logout()
          .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
        .and()
          .csrf().disable();  //Add logout on get (instead off post)
    }
}
