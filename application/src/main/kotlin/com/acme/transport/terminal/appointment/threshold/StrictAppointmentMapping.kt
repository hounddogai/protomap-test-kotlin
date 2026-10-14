package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict appointment mapping is in its life cycle. */
enum class StrictAppointmentMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a strict appointment mapping and its current status. */
data class StrictAppointmentMapping(
    var code: String,
    var title: String,
    var visible: Boolean,
    var externalRef: String,
    var etaMinutes: Int,
    var sequence: Int,
    var status: StrictAppointmentMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAppointmentMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAppointmentMappingStatus): Boolean {
        when (status) {
            StrictAppointmentMappingStatus.PLANNED -> return next == StrictAppointmentMappingStatus.SCHEDULED || next == StrictAppointmentMappingStatus.COMPLETED
            StrictAppointmentMappingStatus.SCHEDULED -> {
                when (next) {
                    StrictAppointmentMappingStatus.IN_PROGRESS, StrictAppointmentMappingStatus.CANCELLED -> return true
                }
                return false
            }
            StrictAppointmentMappingStatus.IN_PROGRESS -> {
                when (next) {
                    StrictAppointmentMappingStatus.COMPLETED, StrictAppointmentMappingStatus.CANCELLED -> return true
                }
                return false
            }
            StrictAppointmentMappingStatus.COMPLETED -> return next == StrictAppointmentMappingStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: StrictAppointmentMappingStatus) {
        if (!canMoveTo(next)) {
            error("strict appointment mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict appointment mapping"} $code"

    fun adjustedExposure(): Double = round((etaMinutes.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): StrictAppointmentMapping {
        return StrictAppointmentMapping(
            code = code,
            title = title,
            visible = visible,
            externalRef = externalRef,
            etaMinutes = etaMinutes,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict appointment mapping in status planned. */
fun newStrictAppointmentMapping(code: String, title: String): StrictAppointmentMapping {
    return StrictAppointmentMapping(
        code = code,
        title = title,
        visible = false,
        externalRef = "",
        etaMinutes = 0,
        sequence = 0,
        status = StrictAppointmentMappingStatus.PLANNED,
    )
}
