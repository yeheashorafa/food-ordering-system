package com.example.restaurantservice.service;

import com.example.restaurantservice.entity.MenuItem;
import com.example.restaurantservice.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MenuItemService {
    private final MenuItemRepository repository;

    public MenuItemService(MenuItemRepository repository) {
        this.repository = repository;
    }

    public List<MenuItem> getMenu() {
        return repository.findAll();
    }

    public MenuItem getItem(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));
    }

    @Transactional
    public MenuItem reserve(Long id, Integer quantity) {
        MenuItem item = getItem(id);
        if (!Boolean.TRUE.equals(item.getAvailable()) || item.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Menu item is not available in requested quantity");
        }
        item.setAvailableQuantity(item.getAvailableQuantity() - quantity);
        if (item.getAvailableQuantity() == 0) {
            item.setAvailable(false);
        }
        return repository.save(item);
    }

    @Transactional
    public MenuItem release(Long id, Integer quantity) {
        MenuItem item = getItem(id);
        item.setAvailableQuantity(item.getAvailableQuantity() + quantity);
        item.setAvailable(true);
        return repository.save(item);
    }
}
