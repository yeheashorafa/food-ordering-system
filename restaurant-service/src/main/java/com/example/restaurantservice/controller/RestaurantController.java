package com.example.restaurantservice.controller;

import com.example.restaurantservice.entity.MenuItem;
import com.example.restaurantservice.service.MenuItemService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    private final MenuItemService menuItemService;

    public RestaurantController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @GetMapping("/menu")
    public List<MenuItem> getMenu() {
        return menuItemService.getMenu();
    }

    @GetMapping("/items/{id}")
    public MenuItem getItem(@PathVariable Long id) {
        return menuItemService.getItem(id);
    }

    @PostMapping("/items/{id}/reserve")
    public MenuItem reserveItem(@PathVariable Long id, @RequestParam Integer quantity) {
        return menuItemService.reserve(id, quantity);
    }

    @PostMapping("/items/{id}/release")
    public MenuItem releaseItem(@PathVariable Long id, @RequestParam Integer quantity) {
        return menuItemService.release(id, quantity);
    }
}
