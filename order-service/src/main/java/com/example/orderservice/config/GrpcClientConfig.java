package com.example.orderservice.config;

import com.example.orderservice.grpc.CustomerGrpcServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {
    private ManagedChannel channel;

    @Bean
    public ManagedChannel customerChannel(@Value("${grpc.customer.host}") String host,
                                          @Value("${grpc.customer.port}") int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        return this.channel;
    }

    @Bean
    public CustomerGrpcServiceGrpc.CustomerGrpcServiceBlockingStub customerGrpcStub(ManagedChannel customerChannel) {
        return CustomerGrpcServiceGrpc.newBlockingStub(customerChannel);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
    }
}
