package com.cityconnect.appointments;

public class SlotAlreadyTakenException extends RuntimeException {

    public SlotAlreadyTakenException(String officeId, java.time.Instant startsAt) {
        super("Horário já reservado no escritório '%s' para %s".formatted(officeId, startsAt));
    }
}
