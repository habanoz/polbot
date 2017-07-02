package com.habanoz.polbot.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/allusers","/collectiveorder/*","/currencyconfig/*","/edituserinfo","/editbotinfo","/editbotuserinfo/*","/mybalances/*","/mycurrencies/*","/orders/*").hasRole("BOT")
                .antMatchers("/tradehistory/*").hasRole("ANALYSIS")
                .antMatchers("/", "/home", "/DataTables/**","/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage("/login").defaultSuccessUrl("/")
                .usernameParameter("username").passwordParameter("password")
                .permitAll()
                .and()
                .logout().logoutSuccessUrl("/")
                .permitAll();
    }


    @Autowired
    DataSource dataSource;

    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {

        auth.jdbcAuthentication().dataSource(dataSource)

                .usersByUsernameQuery(

                        "select user_name,password, active from user where user_name=?")

                .authoritiesByUsernameQuery(

                        "select user_name, role from user_role where user_name=?");

    }
}