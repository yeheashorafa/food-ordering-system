package com.example.orderservice.client;

import com.example.orderservice.dto.CustomerResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomerClient {
    private final RestTemplate restTemplate;
    private final String customerServiceUrl;

    public CustomerClient(RestTemplate restTemplate,
                          @Value("${customer.service.url}") String customerServiceUrl) {
        this.restTemplate = restTemplate;
        this.customerServiceUrl = customerServiceUrl;
    }

    public CustomerResponse getCustomerById(Long customerId) {
        return restTemplate.getForObject(customerServiceUrl + "/api/customers/" + customerId, CustomerResponse.class);
    }
}
