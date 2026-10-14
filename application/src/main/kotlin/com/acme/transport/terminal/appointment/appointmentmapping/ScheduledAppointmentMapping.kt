package com.acme.transport.terminal.appointment.appointmentmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled appointment mapping records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a scheduled appointment mapping is in its life cycle. */
enum class ScheduledAppointmentMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled appointment mapping as the appointment team keeps it. */
data class ScheduledAppointmentMapping(
    var code: String,
    var title: String,
    var driverId: String,
    var discount: Double,
    var rate: Double,
    var channel: String,
    var sequence: Int,
    var odometer: Int,
    var expiresAt: Int,
    var status: ScheduledAppointmentMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAppointmentMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAppointmentMappingStatus): Boolean {
        when (status) {
            ScheduledAppointmentMappingStatus.OPEN -> {
                when (next) {
                    ScheduledAppointmentMappingStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledAppointmentMappingStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledAppointmentMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledAppointmentMappingStatus.RESOLVED -> return next == ScheduledAppointmentMappingStatus.REOPENED
            ScheduledAppointmentMappingStatus.REOPENED -> return next == ScheduledAppointmentMappingStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledAppointmentMappingStatus) {
        if (!canMoveTo(next)) {
            error("scheduled appointment mapping $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled appointment mapping"} $code"

    fun projectedLoad(): Double = round((rate - discount) * 100) / 100

    fun snapshot(): ScheduledAppointmentMapping {
        return ScheduledAppointmentMapping(
            code = code,
            title = title,
            driverId = driverId,
            discount = discount,
            rate = rate,
            channel = channel,
            sequence = sequence,
            odometer = odometer,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a scheduled appointment mapping in status open. */
fun newScheduledAppointmentMapping(code: String, title: String): ScheduledAppointmentMapping {
    return ScheduledAppointmentMapping(
        code = code,
        title = title,
        driverId = "",
        discount = 0.0,
        rate = 0.0,
        channel = "",
        sequence = 0,
        odometer = 0,
        expiresAt = 0,
        status = ScheduledAppointmentMappingStatus.OPEN,
    )
}
