package com.ian.fix.acceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import quickfix.ConfigError;
import quickfix.SessionSettings;

@Configuration
public class QuickfixConfig {

    @Bean
    public SessionSettings sessionSettings() throws ConfigError {
        return new SessionSettings("session.cfg");
    }

}
