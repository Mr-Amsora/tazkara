package com.tazkara.bookingservice.domain;

import java.security.SecureRandom;

public final class BookingReference {

    private static final int LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private BookingReference() {}

    public static String generate() {
        StringBuilder reference = new StringBuilder("TZK-");

        for (int i = 0; i < LENGTH; i++) {
            int type = RANDOM.nextInt(2);

            if (type == 0) {
                reference.append((char) ('A' + RANDOM.nextInt(26)));
            } else {
                reference.append((char) ('0' + RANDOM.nextInt(10)));
            }
        }

        return reference.toString();
    }
}