package com.tazkara.bookingservice.export;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tazkara.bookingservice.domain.Booking;
import com.tazkara.bookingservice.domain.BookingLine;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TicketPdfGenerator {

    private static final Color BRAND_COLOR = new Color(31, 78, 121);
    private static final Color LINE_COLOR = new Color(210, 214, 220);

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.WHITE);
    private static final Font LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
    private static final Font VALUE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);

    public static byte[] generate(Booking booking) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A5.rotate(), 28, 28, 28, 28);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            int totalTickets = booking.getLines().stream().mapToInt(BookingLine::getQuantity).sum();
            int currentTicket = 1;

            for (BookingLine line : booking.getLines()) {
                for (int i = 0; i < line.getQuantity(); i++) {
                    if (currentTicket > 1) {
                        document.newPage();
                    }

                    document.add(createTicketTable(booking, line, currentTicket, totalTickets));
                    document.add(createFooter());

                    currentTicket++;
                }
            }
            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }

        return out.toByteArray();
    }

    private static PdfPTable createTicketTable(Booking booking, BookingLine line, int currentTicket, int totalTickets) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30f, 70f});

        PdfPCell header = new PdfPCell(new Phrase("EVENT TICKET: " + booking.getEventTitle(), TITLE_FONT));
        header.setColspan(2);
        header.setBackgroundColor(BRAND_COLOR);
        header.setBorder(Rectangle.NO_BORDER);
        header.setPadding(14);
        table.addCell(header);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm").withZone(ZoneOffset.UTC);
        String dateStr = formatter.format(booking.getEventStartTime());

        String venueStr = (booking.getEventVenue() != null) ? booking.getEventVenue() + ", " + booking.getEventCity() : "TBD";
        String priceStr = (line.getUnitPrice().signum() == 0) ? "Free" : line.getUnitPrice() + " JOD";
        String ticketNumStr = booking.getReference() + "-" + currentTicket + "  (" + currentTicket + " of " + totalTickets + ")";

        addRow(table, "Date and time", dateStr);
        addRow(table, "Venue", venueStr);
        addRow(table, "Ticket type", line.getTicketTypeName());
        addRow(table, "Price", priceStr);
        addRow(table, "Ticket holder", booking.getAttendeeEmail());
        addRow(table, "Booking reference", booking.getReference());
        addRow(table, "Ticket Number", ticketNumStr);

        return table;
    }

    private static void addRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setBorderColor(LINE_COLOR);
        labelCell.setPadding(6);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, VALUE_FONT));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setBorderColor(LINE_COLOR);
        valueCell.setPadding(6);
        table.addCell(valueCell);
    }

    private static Paragraph createFooter() {
        Font noteFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);
        Paragraph footer = new Paragraph("Please show this ticket at the entrance. One ticket admits one person. Times are in UTC.", noteFont);
        footer.setSpacingBefore(12);
        footer.setAlignment(Element.ALIGN_CENTER);
        return footer;
    }
}