package com.example.orderservice.service;

import com.example.orderservice.dto.CustomerResponse;
import com.example.orderservice.grpc.CustomerGrpcServiceGrpc;
import com.example.orderservice.grpc.CustomerReply;
import com.example.orderservice.grpc.CustomerRequest;
import org.springframework.stereotype.Service;

@Service
public class GrpcCustomerClientService {
    private final CustomerGrpcServiceGrpc.CustomerGrpcServiceBlockingStub customerGrpcStub;

    public GrpcCustomerClientService(CustomerGrpcServiceGrpc.CustomerGrpcServiceBlockingStub customerGrpcStub) {
        this.customerGrpcStub = customerGrpcStub;
    }

    public CustomerResponse getCustomerById(Long customerId) {
        CustomerReply reply = customerGrpcStub.getCustomerById(CustomerRequest.newBuilder().setId(customerId).build());
        return new CustomerResponse(reply.getId(), reply.getName(), reply.getEmail(), reply.getPhone(), reply.getAddress());
    }
}
