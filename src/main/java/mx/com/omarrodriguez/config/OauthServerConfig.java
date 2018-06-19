/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.omarrodriguez.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

/**
 *
 * @author omar
 */
@Configuration
@EnableAuthorizationServer
public class OauthServerConfig extends AuthorizationServerConfigurerAdapter {

  
    /**
     * Authentication Manager configurado en SecurityConf
     */
    @Autowired
    private AuthenticationManager auth;

    /**
     * dataSource se obtiene de la autoconfiguracion de SpringBoot y de los
     * datos que almacenamos en application.security
     */
    @Autowired
    private DataSource dataSource;

    /**
     * Implementacion del almacan de tokens en jdbc, se entrega un bean
     * que implementa la interfaz TokenStore, con acceso directo a la base 
     * de datos
     * @return 
     */
    @Bean
    public JdbcTokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    /**
     * Los codigos de autorizacion es otra interfaz, de esta forma se separan
     * los codigos de los tokens
     * @return 
     */
    @Bean
    protected AuthorizationCodeServices authorizationCodeServices() {
        return new JdbcAuthorizationCodeServices(dataSource);
    }

    
    /**
     * Asignar los permisos de acceso para los accesos a tokens, configurar
     * el algoritmo de cifrado de datos
     * @param security
     * @throws Exception 
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security)
            throws Exception {
        security
                .passwordEncoder(new BCryptPasswordEncoder())
                .tokenKeyAccess("permitAll()")                  //Abrir los endpoints de solicitud de tokens
                .checkTokenAccess("isAuthenticated()");
    }
    
    /**
     * Configurar el comportamiento de los endpoints de auth
     * @param endpoints
     * @throws Exception 
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints)
            throws Exception {
        endpoints
          .authorizationCodeServices(authorizationCodeServices())
          .authenticationManager(auth)
          .tokenStore(tokenStore())
          .approvalStoreDisabled();
    }

    
    /**
     * Metodo proporcionado para realizar la creacion inicial de usuarios
     * @param clients
     * @throws Exception 
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // @formatter:off
        clients.jdbc(dataSource)
                .passwordEncoder(new BCryptPasswordEncoder())
                .withClient("my-client")
                .authorizedGrantTypes("password", "authorization_code",
                        "refresh_token")
                .authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT")
                .scopes("read", "write")
                .secret("secret");
        // @formatter:on
    }

}
