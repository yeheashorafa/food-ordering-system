package com.example.restaurantservice.config;

import com.example.restaurantservice.entity.MenuItem;
import com.example.restaurantservice.repository.MenuItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initMenu(MenuItemRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new MenuItem(1L, "Gaza Burger", "Burger Meal", "Burger, fries and cola", 25.0, 20, true));
                repository.save(new MenuItem(1L, "Gaza Burger", "Chicken Pizza", "Large chicken pizza", 40.0, 15, true));
                repository.save(new MenuItem(2L, "Premium Steak", "Family Steak Box", "High value demo item for failed payment", 750.0, 10, true));
            }
        };
    }
}
