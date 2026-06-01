package com.example.customerservice.config;

import com.example.customerservice.entity.Customer;
import com.example.customerservice.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initCustomers(CustomerRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new Customer("Ahmad Ali", "ahmad@example.com", "0591234567", "Gaza"));
                repository.save(new Customer("Sara Khaled", "sara@example.com", "0597654321", "Gaza"));
            }
        };
    }
}
