package com.cityconnect.appointments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AppointmentServiceTest {

    private final AppointmentService service = new AppointmentService();
    private final Instant slot = Instant.parse("2030-01-01T14:00:00Z");

    @Test
    void createsAndFindsAppointment() {
        Appointment created = service.create("cid-42", "centro", slot);

        assertEquals(created, service.find(created.id()).orElseThrow());
    }

    @Test
    void rejectsSecondBookingForSameOfficeAndTime() {
        service.create("cid-1", "centro", slot);

        assertThrows(SlotAlreadyTakenException.class, () -> service.create("cid-2", "centro", slot));
    }

    @Test
    void allowsSameTimeInDifferentOffice() {
        service.create("cid-1", "centro", slot);

        assertTrue(service.create("cid-2", "norte", slot).id() != null);
    }
}
