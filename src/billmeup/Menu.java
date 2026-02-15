package billmeup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Menu {
    private final List<MenuItem> items;

    public Menu() {
        items = new ArrayList<>();
        seed();
    }

    private void seed() {
        items.add(new MenuItem(101, "Margherita Pizza", "Main Course", 249));
        items.add(new MenuItem(102, "Veg Burger", "Main Course", 149));
        items.add(new MenuItem(103, "Paneer Tikka", "Starters", 219));
        items.add(new MenuItem(104, "French Fries", "Starters", 119));
        items.add(new MenuItem(105, "Cold Coffee", "Beverages", 99));
        items.add(new MenuItem(106, "Masala Chai", "Beverages", 49));
        items.add(new MenuItem(107, "Chocolate Brownie", "Desserts", 129));
        items.add(new MenuItem(108, "Gulab Jamun", "Desserts", 79));
    }

    public List<MenuItem> getAllItems() {
        return Collections.unmodifiableList(items);
    }

    public MenuItem findById(int id) {
        for (MenuItem item : items) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    public void printMenu() {
        System.out.println("\n==================== MENU ====================");
        System.out.printf("%-6s %-24s %-14s %8s%n", "ID", "Item", "Category", "Price");
        System.out.println("----------------------------------------------");
        for (MenuItem item : items) {
            System.out.printf("%-6d %-24s %-14s %8.2f%n",
                    item.getId(), item.getName(), item.getCategory(), item.getPrice());
        }
        System.out.println("==============================================");
    }
}
