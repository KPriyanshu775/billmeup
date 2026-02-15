package billmeup;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Invoice {
    private static int nextBillNo = 1001;

    private final int billNo;
    private final String customerName;
    private final String orderType;
    private final Order order;
    private final double gstPercent;
    private final double serviceChargePercent;
    private final double cash;
    private final double upi;
    private final double card;
    private final LocalDateTime timestamp;

    public Invoice(String customerName,
                   String orderType,
                   Order order,
                   double gstPercent,
                   double serviceChargePercent,
                   double cash,
                   double upi,
                   double card) {
        this.billNo = nextBillNo++;
        this.customerName = customerName;
        this.orderType = orderType;
        this.order = order;
        this.gstPercent = gstPercent;
        this.serviceChargePercent = serviceChargePercent;
        this.cash = cash;
        this.upi = upi;
        this.card = card;
        this.timestamp = LocalDateTime.now();
    }

    public double subtotal() {
        return order.subtotal();
    }

    public double gstAmount() {
        return subtotal() * gstPercent / 100.0;
    }

    public double serviceChargeAmount() {
        return subtotal() * serviceChargePercent / 100.0;
    }

    public double grandTotal() {
        return subtotal() + gstAmount() + serviceChargeAmount();
    }

    public double paidAmount() {
        return cash + upi + card;
    }

    public double dueOrChange() {
        return paidAmount() - grandTotal();
    }

    public void print() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        System.out.println("\n============================================================");
        System.out.println("                        BILLMEUP INVOICE                    ");
        System.out.println("============================================================");
        System.out.printf("Bill No     : %d%n", billNo);
        System.out.printf("Date & Time : %s%n", timestamp.format(formatter));
        System.out.printf("Customer    : %s%n", customerName);
        System.out.printf("Order Type  : %s%n", orderType);
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-5s %-24s %-5s %8s %10s%n", "ID", "Item", "Qty", "Price", "Amount");
        System.out.println("------------------------------------------------------------");

        for (Map.Entry<Integer, OrderItem> entry : order.getItems().entrySet()) {
            OrderItem line = entry.getValue();
            MenuItem item = line.getMenuItem();
            System.out.printf("%-5d %-24s %-5d %8.2f %10.2f%n",
                    item.getId(), item.getName(), line.getQuantity(), item.getPrice(), line.lineTotal());
        }

        System.out.println("------------------------------------------------------------");
        System.out.printf("Subtotal                       : %24.2f%n", subtotal());
        System.out.printf("GST (%.2f%%)                     : %24.2f%n", gstPercent, gstAmount());
        System.out.printf("Service Charge (%.2f%%)          : %24.2f%n", serviceChargePercent, serviceChargeAmount());
        System.out.println("------------------------------------------------------------");
        System.out.printf("GRAND TOTAL                    : %24.2f%n", grandTotal());
        System.out.println("------------------------------------------------------------");
        System.out.printf("Payment - Cash                 : %24.2f%n", cash);
        System.out.printf("Payment - UPI                  : %24.2f%n", upi);
        System.out.printf("Payment - Card                 : %24.2f%n", card);
        System.out.printf("Total Paid                     : %24.2f%n", paidAmount());

        if (dueOrChange() < 0) {
            System.out.printf("Amount Due                     : %24.2f%n", -dueOrChange());
        } else {
            System.out.printf("Change Return                  : %24.2f%n", dueOrChange());
        }

        System.out.println("============================================================");
        System.out.println("Thank you for dining with us!");
        System.out.println("============================================================");
    }
}
