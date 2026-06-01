package com.example.customerservice.config;

import com.example.customerservice.grpcservice.CustomerGrpcServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

@Configuration
public class GrpcServerConfig {
    private final Server server;

    public GrpcServerConfig(CustomerGrpcServiceImpl customerGrpcService,
                            @Value("${grpc.server.port}") int grpcPort) throws IOException {
        this.server = ServerBuilder.forPort(grpcPort)
                .addService(customerGrpcService)
                .build()
                .start();
        System.out.println("Customer gRPC server started on port " + grpcPort);
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}
