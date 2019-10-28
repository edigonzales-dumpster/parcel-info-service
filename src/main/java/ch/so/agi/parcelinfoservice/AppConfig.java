package ch.so.agi.parcelinfoservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class AppConfig {
    /*
    @Bean
    public ObjectMapper myObjectMapper() {
      return new ObjectMapper().registerModule(new JtsModule());
    }
    */
}
