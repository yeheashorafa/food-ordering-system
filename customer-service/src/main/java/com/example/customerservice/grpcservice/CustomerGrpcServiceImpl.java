package com.example.customerservice.grpcservice;

import com.example.customerservice.dto.CustomerResponse;
import com.example.customerservice.grpc.CustomerGrpcServiceGrpc;
import com.example.customerservice.grpc.CustomerReply;
import com.example.customerservice.grpc.CustomerRequest;
import com.example.customerservice.service.CustomerService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class CustomerGrpcServiceImpl extends CustomerGrpcServiceGrpc.CustomerGrpcServiceImplBase {
    private final CustomerService customerService;

    public CustomerGrpcServiceImpl(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public void getCustomerById(CustomerRequest request, StreamObserver<CustomerReply> responseObserver) {
        try {
            CustomerResponse customer = customerService.getById(request.getId());
            CustomerReply reply = CustomerReply.newBuilder()
                    .setId(customer.getId())
                    .setName(customer.getName())
                    .setEmail(customer.getEmail())
                    .setPhone(customer.getPhone())
                    .setAddress(customer.getAddress())
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Customer not found with id: " + request.getId())
                    .asRuntimeException());
        }
    }
}
