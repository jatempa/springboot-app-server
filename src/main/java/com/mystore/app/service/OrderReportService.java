package com.mystore.app.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.mystore.app.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class OrderReportService {

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    private static final Color HEADER_BG = new Color(0x33, 0x33, 0x33);
    private static final Color ROW_ALT_BG = new Color(0xF5, 0xF5, 0xF5);
    private static final Color SECTION_LABEL_COLOR = new Color(0x44, 0x44, 0x44);

    @Value("${app.reports.directory:reports}")
    private String reportsDirectory;

    public Path generateReport(OrderResponseDTO order) {
        Path reportPath = resolveReportPath(order.getOrderId());
        try {
            Files.createDirectories(reportPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Cannot create reports directory", e);
        }

        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(reportPath.toFile()));
            document.open();

            addHeader(document, order.getOrderId());
            document.add(Chunk.NEWLINE);
            addClientSection(document, order.getClient());
            document.add(Chunk.NEWLINE);
            addOrderSection(document, order);
            document.add(Chunk.NEWLINE);
            addItemsTable(document, order.getItems());
            document.add(Chunk.NEWLINE);
            addPaymentsTable(document, order.getPayments());
            document.add(Chunk.NEWLINE);
            addSummaryFooter(document, order);

            document.close(); // flushes and closes the underlying FileOutputStream
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Failed to generate PDF for order " + order.getOrderId(), e);
        }

        log.info("Report generated: {}", reportPath);
        return reportPath;
    }

    public Path resolveReportPath(Integer orderId) {
        return Paths.get(reportsDirectory, "order-" + orderId + ".pdf");
    }

    // ── Sections ─────────────────────────────────────────────────────────────

    private void addHeader(Document doc, Integer orderId) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLACK);
        Paragraph title = new Paragraph("Order Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 11, SECTION_LABEL_COLOR);
        Paragraph sub = new Paragraph(
                "Order #" + orderId + "  ·  Generated " + DATE_TIME_FMT.format(Instant.now()),
                subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        doc.add(sub);
    }

    private void addClientSection(Document doc, ClientResponseDTO client) throws DocumentException {
        doc.add(sectionTitle("Client Details"));
        PdfPTable table = noGridTable(2, new float[]{35f, 65f});
        String regionName = (client.getRegion() != null) ? client.getRegion().getRegionName() : "—";
        addLabelValue(table, "Client ID",     String.valueOf(client.getClientId()));
        addLabelValue(table, "Name",          client.getFirstName() + " " + client.getLastName());
        addLabelValue(table, "Email",         client.getEmail());
        addLabelValue(table, "Phone",         nvl(client.getPhone()));
        addLabelValue(table, "City",          nvl(client.getCity()));
        addLabelValue(table, "Region",        regionName);
        addLabelValue(table, "Segment",       nvl(client.getSegment()));
        addLabelValue(table, "Loyalty Points",String.valueOf(client.getLoyaltyPts()));
        doc.add(table);
    }

    private void addOrderSection(Document doc, OrderResponseDTO order) throws DocumentException {
        doc.add(sectionTitle("Order Details"));
        PdfPTable table = noGridTable(2, new float[]{35f, 65f});
        addLabelValue(table, "Order Date",    formatDateTime(order.getOrderDate()));
        addLabelValue(table, "Ship Date",     order.getShipDate() != null ? formatDateTime(order.getShipDate()) : "—");
        addLabelValue(table, "Status",        nvl(order.getStatus()));
        addLabelValue(table, "Channel",       nvl(order.getChannel()));
        addLabelValue(table, "Discount",      pct(order.getDiscountPct()));
        addLabelValue(table, "Shipping Cost", money(order.getShippingCost()));
        addLabelValue(table, "Notes",         nvl(order.getNotes()));
        doc.add(table);
    }

    private void addItemsTable(Document doc, List<OrderItemResponseDTO> items) throws DocumentException {
        doc.add(sectionTitle("Order Items"));

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30f, 8f, 14f, 12f, 14f, 14f});

        addTableHeader(table, "Product", "Qty", "Unit Price", "Discount", "Line Discount", "Line Total");

        boolean alt = false;
        for (OrderItemResponseDTO item : items) {
            String productName = (item.getProduct() != null) ? item.getProduct().getProductName() : "—";
            BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal discPct = item.getDiscountPct() != null ? item.getDiscountPct() : BigDecimal.ZERO;
            BigDecimal gross = unitPrice.multiply(qty);
            BigDecimal lineDiscount = gross.multiply(discPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = gross.subtract(lineDiscount);

            Color bg = alt ? ROW_ALT_BG : Color.WHITE;
            addDataRow(table, bg,
                    productName,
                    String.valueOf(item.getQuantity()),
                    money(unitPrice),
                    pct(discPct),
                    money(lineDiscount),
                    money(lineTotal));
            alt = !alt;
        }

        doc.add(table);
    }

    private void addPaymentsTable(Document doc, List<PaymentResponseDTO> payments) throws DocumentException {
        doc.add(sectionTitle("Payments"));

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{15f, 22f, 20f, 22f, 21f});

        addTableHeader(table, "Payment ID", "Date", "Amount", "Method", "Status");

        boolean alt = false;
        for (PaymentResponseDTO p : payments) {
            Color bg = alt ? ROW_ALT_BG : Color.WHITE;
            addDataRow(table, bg,
                    String.valueOf(p.getPaymentId()),
                    p.getPaidAt() != null ? DATE_FMT.format(p.getPaidAt()) : "—",
                    money(p.getAmount()),
                    nvl(p.getMethod()),
                    nvl(p.getStatus()));
            alt = !alt;
        }

        doc.add(table);
    }

    private void addSummaryFooter(Document doc, OrderResponseDTO order) throws DocumentException {
        doc.add(sectionTitle("Summary"));

        List<OrderItemResponseDTO> items = order.getItems();
        BigDecimal subtotal = items.stream()
                .map(i -> safe(i.getUnitPrice()).multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal itemDiscounts = items.stream()
                .map(i -> {
                    BigDecimal gross = safe(i.getUnitPrice()).multiply(BigDecimal.valueOf(i.getQuantity()));
                    BigDecimal pct = safe(i.getDiscountPct());
                    return gross.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal orderDiscountPct = safe(order.getDiscountPct());
        BigDecimal orderDiscount = subtotal.subtract(itemDiscounts)
                .multiply(orderDiscountPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal shipping = safe(order.getShippingCost());
        BigDecimal grandTotal = subtotal.subtract(itemDiscounts).subtract(orderDiscount).add(shipping);

        BigDecimal totalPaid = order.getPayments().stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()))
                .map(p -> safe(p.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PdfPTable table = new PdfPTable(2);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setWidthPercentage(45);
        table.setWidths(new float[]{55f, 45f});

        addSummaryRow(table, "Subtotal",              money(subtotal),       false);
        addSummaryRow(table, "Item Discounts",         "−" + money(itemDiscounts), false);
        addSummaryRow(table, "Order Discount (" + pct(orderDiscountPct) + ")", "−" + money(orderDiscount), false);
        addSummaryRow(table, "Shipping Cost",          money(shipping),       false);
        addSummaryRow(table, "Grand Total",            money(grandTotal),     true);
        addSummaryRow(table, "Total Paid",             money(totalPaid),      false);

        doc.add(table);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Paragraph sectionTitle(String text) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, SECTION_LABEL_COLOR);
        Paragraph p = new Paragraph(text, f);
        p.setSpacingBefore(6f);
        p.setSpacingAfter(4f);
        return p;
    }

    private PdfPTable noGridTable(int cols, float[] widths) throws DocumentException {
        PdfPTable table = new PdfPTable(cols);
        table.setWidthPercentage(100);
        table.setWidths(widths);
        return table;
    }

    private void addLabelValue(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, SECTION_LABEL_COLOR);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(3f);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(3f);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, f));
            cell.setBackgroundColor(HEADER_BG);
            cell.setPadding(5f);
            cell.setBorderColor(Color.DARK_GRAY);
            table.addCell(cell);
        }
    }

    private void addDataRow(PdfPTable table, Color bg, String... values) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        for (String v : values) {
            PdfPCell cell = new PdfPCell(new Phrase(v, f));
            cell.setBackgroundColor(bg);
            cell.setPadding(4f);
            cell.setBorderColor(new Color(0xCC, 0xCC, 0xCC));
            table.addCell(cell);
        }
    }

    private void addSummaryRow(PdfPTable table, String label, String value, boolean bold) {
        int style = bold ? Font.BOLD : Font.NORMAL;
        Font f = FontFactory.getFont(FontFactory.HELVETICA, 10, style, Color.BLACK);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, f));
        PdfPCell valueCell = new PdfPCell(new Phrase(value, f));

        if (bold) {
            labelCell.setBorderWidthTop(1.5f);
            valueCell.setBorderWidthTop(1.5f);
        } else {
            labelCell.setBorder(Rectangle.NO_BORDER);
            valueCell.setBorder(Rectangle.NO_BORDER);
        }

        labelCell.setPaddingTop(bold ? 5f : 2f);
        labelCell.setPaddingBottom(2f);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPaddingTop(bold ? 5f : 2f);
        valueCell.setPaddingBottom(2f);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String money(BigDecimal val) {
        if (val == null) return "$0.00";
        return "$" + val.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String pct(BigDecimal val) {
        if (val == null) return "0%";
        return val.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "%";
    }

    private String nvl(String val) {
        return (val != null && !val.isBlank()) ? val : "—";
    }

    private String formatDateTime(Instant instant) {
        return instant != null ? DATE_TIME_FMT.format(instant) : "—";
    }

    private BigDecimal safe(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
