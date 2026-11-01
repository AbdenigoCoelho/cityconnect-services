package com.cityconnect.appointments;

import java.time.Instant;

public record Appointment(
        String id,
        String citizenId,
        String officeId,
        Instant startsAt,
        Instant createdAt) {
}
