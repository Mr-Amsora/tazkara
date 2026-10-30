package com.tazkara.bookingservice.export;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tazkara.bookingservice.domain.Booking;
import com.tazkara.bookingservice.domain.BookingLine;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class TicketPdfGenerator {

    private static final String CURRENCY = "JOD";

    private static final Color BRAND = new Color(31, 78, 121);
    private static final Color LINE = new Color(210, 214, 220);

    private static final Font BRAND_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.WHITE);
    private static final Font LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
    private static final Font VALUE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
    private static final Font NOTE_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm 'UTC'").withZone(ZoneOffset.UTC);

    private TicketPdfGenerator() {}

    public static byte[] generate(Booking booking) {
        int totalTickets = booking.getLines().stream().mapToInt(BookingLine::getQuantity).sum();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A5.rotate(), 28, 28, 28, 28);

        try {
            PdfWriter.getInstance(document, out);
            document.addTitle("Tickets " + booking.getReference());
            document.addAuthor("Tazkara");
            document.open();

            int number = 0;
            for (BookingLine line : booking.getLines()) {
                for (int i = 0; i < line.getQuantity(); i++) {
                    number++;
                    if (number > 1) {
                        document.newPage();
                    }
                    document.add(ticketTable(booking, line, number, totalTickets));
                    document.add(footer());
                }
            }
            document.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("Could not generate the ticket PDF", e);
        }

        return out.toByteArray();
    }

    private static PdfPTable ticketTable(Booking booking, BookingLine line, int number, int total)
            throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30f, 70f});

        PdfPCell header = new PdfPCell();
        header.setColspan(2);
        header.setBackgroundColor(BRAND);
        header.setBorder(Rectangle.NO_BORDER);
        header.setPadding(14);
        header.addElement(new Paragraph("TAZKARA  |  EVENT TICKET", BRAND_FONT));
        header.addElement(new Paragraph(booking.getEventTitle(), TITLE_FONT));
        table.addCell(header);

        addRow(table, "Date and time", DATE_TIME.format(booking.getEventStartTime()));
        addRow(table, "Venue", venueText(booking));
        addRow(table, "Ticket type", line.getTicketTypeName());
        addRow(table, "Price", priceText(line));
        addRow(table, "Ticket holder", booking.getAttendeeEmail());
        addRow(table, "Booking reference", booking.getReference());
        addRow(table, "Ticket", ticketNumber(booking, number) + "   (" + number + " of " + total + ")");

        return table;
    }

    private static void addRow(PdfPTable table, String label, String value) {
        table.addCell(cell(label, LABEL_FONT));
        table.addCell(cell(value, VALUE_FONT));
    }

    private static PdfPCell cell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(LINE);
        cell.setPadding(6);
        return cell;
    }

    private static Paragraph footer() {
        Paragraph note = new Paragraph(
                "Please show this ticket at the entrance. One ticket admits one person. Times are in UTC.",
                NOTE_FONT);
        note.setSpacingBefore(12);
        note.setAlignment(Element.ALIGN_CENTER);
        return note;
    }

    private static String ticketNumber(Booking booking, int number) {
        return booking.getReference() + "-" + String.format("%02d", number);
    }

    private static String priceText(BookingLine line) {
        if (line.getUnitPrice().signum() == 0) {
            return "Free";
        }
        return line.getUnitPrice().toPlainString() + " " + CURRENCY;
    }

    private static String venueText(Booking booking) {
        String venue = booking.getEventVenue();
        String city = booking.getEventCity();
        if (venue == null || city == null) {
            return "-";
        }
        return venue + ", " + city;
    }
}