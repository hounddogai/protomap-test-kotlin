package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict appointment entry is in its life cycle. */
enum class StrictAppointmentEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A strict appointment entry as the appointment team keeps it. */
data class StrictAppointmentEntry(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var active: Boolean,
    var sequence: Int,
    var stopCount: Int,
    var floorValue: Double,
    var status: StrictAppointmentEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAppointmentEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAppointmentEntryStatus): Boolean {
        when (status) {
            StrictAppointmentEntryStatus.CREATED -> return next == StrictAppointmentEntryStatus.RETIRED
            StrictAppointmentEntryStatus.VALIDATED -> return next == StrictAppointmentEntryStatus.PUBLISHED || next == StrictAppointmentEntryStatus.RETIRED
            StrictAppointmentEntryStatus.PUBLISHED -> return next == StrictAppointmentEntryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: StrictAppointmentEntryStatus) {
        if (!canMoveTo(next)) {
            error("strict appointment entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict appointment entry"} $code"

    fun netValue(): Double = round((sequence.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): StrictAppointmentEntry {
        return StrictAppointmentEntry(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            active = active,
            sequence = sequence,
            stopCount = stopCount,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict appointment entry in status created. */
fun newStrictAppointmentEntry(code: String, label: String): StrictAppointmentEntry {
    return StrictAppointmentEntry(
        code = code,
        label = label,
        fuelLiters = 0.0,
        active = false,
        sequence = 0,
        stopCount = 0,
        floorValue = 0.0,
        status = StrictAppointmentEntryStatus.CREATED,
    )
}
