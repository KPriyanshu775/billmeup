package billmeup;

import java.util.LinkedHashMap;
import java.util.Map;

public class Order {
    private final Map<Integer, OrderItem> cart = new LinkedHashMap<>();

    public void addItem(MenuItem item, int quantity) {
        OrderItem existing = cart.get(item.getId());
        if (existing == null) {
            cart.put(item.getId(), new OrderItem(item, quantity));
        } else {
            existing.setQuantity(existing.getQuantity() + quantity);
        }
    }

    public boolean updateQuantity(int itemId, int quantity) {
        OrderItem existing = cart.get(itemId);
        if (existing == null) {
            return false;
        }
        if (quantity <= 0) {
            cart.remove(itemId);
        } else {
            existing.setQuantity(quantity);
        }
        return true;
    }

    public boolean removeItem(int itemId) {
        return cart.remove(itemId) != null;
    }

    public boolean isEmpty() {
        return cart.isEmpty();
    }

    public Map<Integer, OrderItem> getItems() {
        return cart;
    }

    public double subtotal() {
        double total = 0;
        for (OrderItem item : cart.values()) {
            total += item.lineTotal();
        }
        return total;
    }

    public void printCurrentCart() {
        if (cart.isEmpty()) {
            System.out.println("\nCart is empty.");
            return;
        }

        System.out.println("\n------------------ CURRENT CART ------------------");
        System.out.printf("%-6s %-22s %-6s %8s %10s%n", "ID", "Item", "Qty", "Price", "Line Total");
        System.out.println("--------------------------------------------------");

        for (OrderItem orderItem : cart.values()) {
            MenuItem item = orderItem.getMenuItem();
            System.out.printf("%-6d %-22s %-6d %8.2f %10.2f%n",
                    item.getId(), item.getName(), orderItem.getQuantity(), item.getPrice(), orderItem.lineTotal());
        }
        System.out.println("--------------------------------------------------");
        System.out.printf("Subtotal: %.2f%n", subtotal());
    }
}
