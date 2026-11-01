package com.cityconnect.appointments;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Armazenamento em memória apenas para demonstração: reiniciar ou replicar o serviço
 * perde dados / cria visões diferentes. Substituir por banco durável com restrição de
 * unicidade em (officeId, startsAt).
 */
@Service
public class AppointmentService {

    private final ConcurrentHashMap<String, Appointment> byId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> slots = new ConcurrentHashMap<>();

    public Appointment create(String citizenId, String officeId, Instant startsAt) {
        String id = UUID.randomUUID().toString();
        String slotKey = officeId + "|" + startsAt;

        // putIfAbsent é atômico: garante um único agendamento por escritório e horário.
        if (slots.putIfAbsent(slotKey, id) != null) {
            throw new SlotAlreadyTakenException(officeId, startsAt);
        }
        Appointment appointment = new Appointment(id, citizenId, officeId, startsAt, Instant.now());
        byId.put(id, appointment);
        return appointment;
    }

    public Optional<Appointment> find(String id) {
        return Optional.ofNullable(byId.get(id));
    }
}
