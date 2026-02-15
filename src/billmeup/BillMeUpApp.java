package billmeup;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BillMeUpApp extends JFrame {
    private static final double GST_PERCENT = 5.0;

    private final Menu menu;
    private final Order order;

    private final JTextField customerField;
    private final JComboBox<String> orderTypeCombo;
    private final JTable menuTable;
    private final JTable cartTable;
    private final JSpinner qtySpinner;
    private final JCheckBox serviceChargeCheck;

    private final JTextField cashField;
    private final JTextField upiField;
    private final JTextField cardField;

    private final JLabel subtotalLabel;
    private final JLabel gstLabel;
    private final JLabel serviceLabel;
    private final JLabel totalLabel;
    private final JLabel paidLabel;
    private final JLabel dueOrChangeLabel;

    public BillMeUpApp() {
        super("BillMeUp - Restaurant Billing Software");
        this.menu = new Menu();
        this.order = new Order();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new GridLayout(2, 1, 8, 8));
        JPanel customerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customerPanel.add(new JLabel("Customer Name:"));
        customerField = new JTextField(18);
        customerPanel.add(customerField);

        customerPanel.add(Box.createHorizontalStrut(20));
        customerPanel.add(new JLabel("Order Type:"));
        orderTypeCombo = new JComboBox<>(new String[]{"Dine In", "Takeaway"});
        customerPanel.add(orderTypeCombo);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addPanel.add(new JLabel("Quantity:"));
        qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        addPanel.add(qtySpinner);

        JButton addBtn = new JButton("Add Selected Item");
        addBtn.addActionListener(e -> addSelectedItemToCart());
        addPanel.add(addBtn);

        JButton updateBtn = new JButton("Update Selected Cart Qty");
        updateBtn.addActionListener(e -> updateSelectedCartQty());
        addPanel.add(updateBtn);

        JButton removeBtn = new JButton("Remove Selected Cart Item");
        removeBtn.addActionListener(e -> removeSelectedCartItem());
        addPanel.add(removeBtn);

        top.add(customerPanel);
        top.add(addPanel);

        String[] menuCols = {"ID", "Item", "Category", "Price"};
        DefaultTableModel menuModel = new DefaultTableModel(menuCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (MenuItem item : menu.getAllItems()) {
            menuModel.addRow(new Object[]{item.getId(), item.getName(), item.getCategory(), item.getPrice()});
        }
        menuTable = new JTable(menuModel);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        String[] cartCols = {"ID", "Item", "Qty", "Price", "Line Total"};
        DefaultTableModel cartModel = new DefaultTableModel(cartCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartModel);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                wrap("Menu", new JScrollPane(menuTable)),
                wrap("Cart", new JScrollPane(cartTable))
        );
        splitPane.setDividerLocation(540);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));

        JPanel paymentPanel = new JPanel(new GridLayout(3, 4, 8, 8));
        paymentPanel.setBorder(BorderFactory.createTitledBorder("Charges & Payment"));

        serviceChargeCheck = new JCheckBox("Apply 10% Service Charge");
        serviceChargeCheck.addActionListener(e -> refreshTotals());

        paymentPanel.add(serviceChargeCheck);
        paymentPanel.add(new JLabel());
        paymentPanel.add(new JLabel());
        paymentPanel.add(new JLabel());

        paymentPanel.add(new JLabel("Cash:"));
        cashField = new JTextField("0");
        paymentPanel.add(cashField);
        paymentPanel.add(new JLabel("UPI:"));
        upiField = new JTextField("0");
        paymentPanel.add(upiField);

        paymentPanel.add(new JLabel("Card:"));
        cardField = new JTextField("0");
        paymentPanel.add(cardField);

        JButton recalcBtn = new JButton("Recalculate");
        recalcBtn.addActionListener(e -> refreshTotals());
        paymentPanel.add(recalcBtn);

        JButton billBtn = new JButton("Generate Invoice");
        billBtn.addActionListener(e -> showInvoiceDialog());
        paymentPanel.add(billBtn);

        JPanel summaryPanel = new JPanel(new GridLayout(6, 2, 8, 6));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Bill Summary"));

        subtotalLabel = new JLabel("0.00");
        gstLabel = new JLabel("0.00");
        serviceLabel = new JLabel("0.00");
        totalLabel = new JLabel("0.00");
        paidLabel = new JLabel("0.00");
        dueOrChangeLabel = new JLabel("0.00");

        summaryPanel.add(new JLabel("Subtotal:"));
        summaryPanel.add(subtotalLabel);
        summaryPanel.add(new JLabel("GST (5%):"));
        summaryPanel.add(gstLabel);
        summaryPanel.add(new JLabel("Service Charge:"));
        summaryPanel.add(serviceLabel);
        summaryPanel.add(new JLabel("Grand Total:"));
        summaryPanel.add(totalLabel);
        summaryPanel.add(new JLabel("Total Paid:"));
        summaryPanel.add(paidLabel);
        summaryPanel.add(new JLabel("Due (-) / Change (+):"));
        summaryPanel.add(dueOrChangeLabel);

        bottom.add(paymentPanel, BorderLayout.CENTER);
        bottom.add(summaryPanel, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        refreshCartTable();
    }

    private JPanel wrap(String title, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private void addSelectedItemToCart() {
        int row = menuTable.getSelectedRow();
        if (row < 0) {
            showError("Select an item from Menu table.");
            return;
        }

        int id = (int) menuTable.getValueAt(row, 0);
        int qty = (int) qtySpinner.getValue();
        MenuItem item = menu.findById(id);
        if (item == null) {
            showError("Selected menu item not found.");
            return;
        }

        order.addItem(item, qty);
        refreshCartTable();
    }

    private void updateSelectedCartQty() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            showError("Select an item from Cart table.");
            return;
        }

        int id = (int) cartTable.getValueAt(row, 0);
        int qty = (int) qtySpinner.getValue();
        order.updateQuantity(id, qty);
        refreshCartTable();
    }

    private void removeSelectedCartItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            showError("Select an item from Cart table.");
            return;
        }

        int id = (int) cartTable.getValueAt(row, 0);
        order.removeItem(id);
        refreshCartTable();
    }

    private void refreshCartTable() {
        DefaultTableModel model = (DefaultTableModel) cartTable.getModel();
        model.setRowCount(0);

        for (OrderItem line : order.getItems().values()) {
            MenuItem item = line.getMenuItem();
            model.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    line.getQuantity(),
                    format(item.getPrice()),
                    format(line.lineTotal())
            });
        }
        refreshTotals();
    }

    private void refreshTotals() {
        double subtotal = order.subtotal();
        double gst = subtotal * GST_PERCENT / 100.0;
        double service = serviceChargeCheck.isSelected() ? subtotal * 0.10 : 0.0;
        double total = subtotal + gst + service;

        double cash = parseMoney(cashField.getText());
        double upi = parseMoney(upiField.getText());
        double card = parseMoney(cardField.getText());
        double paid = cash + upi + card;
        double diff = paid - total;

        subtotalLabel.setText(format(subtotal));
        gstLabel.setText(format(gst));
        serviceLabel.setText(format(service));
        totalLabel.setText(format(total));
        paidLabel.setText(format(paid));
        dueOrChangeLabel.setText(format(diff));
    }

    private void showInvoiceDialog() {
        if (order.isEmpty()) {
            showError("Cart is empty. Add items before generating invoice.");
            return;
        }

        refreshTotals();

        String customer = customerField.getText().trim();
        if (customer.isEmpty()) {
            customer = "Walk-in Customer";
        }

        String orderType = (String) orderTypeCombo.getSelectedItem();

        double subtotal = order.subtotal();
        double gst = subtotal * GST_PERCENT / 100.0;
        double service = serviceChargeCheck.isSelected() ? subtotal * 0.10 : 0.0;
        double total = subtotal + gst + service;

        double cash = parseMoney(cashField.getText());
        double upi = parseMoney(upiField.getText());
        double card = parseMoney(cardField.getText());
        double paid = cash + upi + card;
        double diff = paid - total;

        StringBuilder bill = new StringBuilder();
        bill.append("================ BILLMEUP INVOICE ================\n");
        bill.append("Date & Time: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                .append("\n");
        bill.append("Customer: ").append(customer).append("\n");
        bill.append("Order Type: ").append(orderType).append("\n");
        bill.append("---------------------------------------------------\n");
        bill.append(String.format("%-5s %-18s %-5s %-8s %-10s%n", "ID", "Item", "Qty", "Price", "Amount"));
        bill.append("---------------------------------------------------\n");

        for (OrderItem line : order.getItems().values()) {
            MenuItem item = line.getMenuItem();
            bill.append(String.format("%-5d %-18s %-5d %-8.2f %-10.2f%n",
                    item.getId(), item.getName(), line.getQuantity(), item.getPrice(), line.lineTotal()));
        }

        bill.append("---------------------------------------------------\n");
        bill.append(String.format("Subtotal: %.2f%n", subtotal));
        bill.append(String.format("GST (5%%): %.2f%n", gst));
        bill.append(String.format("Service Charge: %.2f%n", service));
        bill.append(String.format("Grand Total: %.2f%n", total));
        bill.append("---------------------------------------------------\n");
        bill.append(String.format("Cash: %.2f%n", cash));
        bill.append(String.format("UPI: %.2f%n", upi));
        bill.append(String.format("Card: %.2f%n", card));
        bill.append(String.format("Total Paid: %.2f%n", paid));
        if (diff < 0) {
            bill.append(String.format("Amount Due: %.2f%n", -diff));
        } else {
            bill.append(String.format("Change Return: %.2f%n", diff));
        }
        bill.append("===================================================\n");

        JTextArea textArea = new JTextArea(bill.toString(), 24, 52);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(this, scrollPane, "Invoice", JOptionPane.INFORMATION_MESSAGE);
    }

    private double parseMoney(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            double parsed = Double.parseDouble(value.trim());
            return Math.max(parsed, 0);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String format(double amount) {
        return String.format("%.2f", amount);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }
}
