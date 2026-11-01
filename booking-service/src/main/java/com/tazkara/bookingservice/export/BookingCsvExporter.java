package com.tazkara.bookingservice.export;

import com.tazkara.bookingservice.domain.Booking;
import com.tazkara.bookingservice.domain.BookingLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BookingCsvExporter {

    private static final String HEADER = "reference,attendee_email,ticket_type,quantity,unit_price,line_total,confirmed_at";
    private static final String LINE_END = "\r\n";

    private BookingCsvExporter() {}

    public static byte[] export(List<Booking> confirmedBookings) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (Writer writer = new OutputStreamWriter(bytes, StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            writer.write(HEADER + LINE_END);

            for (Booking booking : confirmedBookings) {
                for (BookingLine line : booking.getLines()) {
                    writer.write(row(booking, line) + LINE_END);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to export CSV", e);
        }

        return bytes.toByteArray();
    }

    private static String row(Booking booking, BookingLine line) {
        return Stream.of(
                        booking.getReference(),
                        booking.getAttendeeEmail(),
                        line.getTicketTypeName(),
                        line.getQuantity(),
                        line.getUnitPrice() != null ? line.getUnitPrice().toPlainString() : null,
                        line.lineTotal() != null ? line.lineTotal().toPlainString() : null,
                        booking.getPaidAt())
                .map(BookingCsvExporter::formatField)
                .collect(Collectors.joining(","));
    }

    private static String formatField(Object field) {
        if (field == null) {
            return "";
        }

        String value = field.toString();

        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}