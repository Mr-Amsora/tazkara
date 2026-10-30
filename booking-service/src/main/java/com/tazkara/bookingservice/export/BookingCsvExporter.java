package com.tazkara.bookingservice.export;

import com.tazkara.bookingservice.domain.Booking;
import com.tazkara.bookingservice.domain.BookingLine;

import java.io.BufferedWriter;
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

    private static final String HEADER =
            "reference,attendee_email,ticket_type,quantity,unit_price,line_total,confirmed_at";
    private static final String LINE_END = "\r\n";
    private static final char BYTE_ORDER_MARK = '\uFEFF';

    private BookingCsvExporter() {}

    public static byte[] export(List<Booking> confirmedBookings) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(bytes, StandardCharsets.UTF_8))) {
            writer.write(BYTE_ORDER_MARK);
            writer.write(HEADER);
            writer.write(LINE_END);

            for (Booking booking : confirmedBookings) {
                for (BookingLine line : booking.getLines()) {
                    writer.write(row(booking, line));
                    writer.write(LINE_END);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return bytes.toByteArray();
    }

    private static String row(Booking booking, BookingLine line) {
        return Stream.of(
                        booking.getReference(),
                        booking.getAttendeeEmail(),
                        line.getTicketTypeName(),
                        String.valueOf(line.getQuantity()),
                        line.getUnitPrice().toPlainString(),
                        line.lineTotal().toPlainString(),
                        booking.getPaidAt().toString())
                .map(BookingCsvExporter::escape)
                .collect(Collectors.joining(","));
    }

    static String escape(String value) {
        String safe = startsLikeFormula(value) ? "'" + value : value;

        boolean needsQuotes = safe.contains(",") || safe.contains("\"")
                || safe.contains("\n") || safe.contains("\r");
        if (!needsQuotes) {
            return safe;
        }
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private static boolean startsLikeFormula(String value) {
        return !value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0;
    }
}