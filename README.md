# BillMeUp (Restaurant Billing Software)

BillMeUp includes:
- Java Swing desktop app (`src/billmeup`)
- Modern web app (`web-billmeup`) for attractive POS-style billing

## Web App Features (Recommended for Demo)
- Restaurant branding and customer/order details
- Real dish cards with images and category filters
- Cart management (add, remove, quantity update)
- GST + optional service charge
- Split payment (cash/UPI/card)
- Realistic tax invoice format (CGST/SGST, bill no, timestamp)
- Invoice print + PDF download
- Local persistence (`localStorage`) for current bill state
- Sales history panel with invoice re-open
- Validation and toast notifications

## Run Web App
Use any one:

1. VS Code Live Server
- Open `web-billmeup/index.html`
- Click `Open with Live Server`

2. XAMPP Apache
- Copy `web-billmeup` into XAMPP `htdocs`
- Start Apache
- Open `http://localhost/web-billmeup`

## Java Desktop Run (Optional)
```bash
javac -d out src/billmeup/*.java
java -cp out billmeup.Main
```

## Suggested Demo Flow
1. Enter customer, mobile, table, server
2. Add menu items and adjust quantity
3. Add service charge and payment split
4. Generate invoice
5. Print or download PDF
6. Show saved sales history item
