package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.text.normalize

/** Where a scheduled appointment estimate is in its life cycle. */
enum class ScheduledAppointmentEstimateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled appointment estimate as the appointment team keeps it. */
data class ScheduledAppointmentEstimate(
    var code: String,
    var name: String,
    var lane: String,
    var startsAt: Int,
    var locale: String,
    var vehicleId: String,
    var status: ScheduledAppointmentEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAppointmentEstimateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAppointmentEstimateStatus): Boolean {
        when (status) {
            ScheduledAppointmentEstimateStatus.DRAFT -> {
                when (next) {
                    ScheduledAppointmentEstimateStatus.PENDING -> return true
                }
                return false
            }
            ScheduledAppointmentEstimateStatus.PENDING -> return next == ScheduledAppointmentEstimateStatus.SUSPENDED || next == ScheduledAppointmentEstimateStatus.CLOSED
            ScheduledAppointmentEstimateStatus.ACTIVE -> return next == ScheduledAppointmentEstimateStatus.SUSPENDED
            ScheduledAppointmentEstimateStatus.SUSPENDED -> {
                when (next) {
                    ScheduledAppointmentEstimateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledAppointmentEstimateStatus) {
        if (!canMoveTo(next)) {
            error("scheduled appointment estimate $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): ScheduledAppointmentEstimate {
        return ScheduledAppointmentEstimate(
            code = code,
            name = name,
            lane = lane,
            startsAt = startsAt,
            locale = locale,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled appointment estimate in status draft. */
fun newScheduledAppointmentEstimate(code: String, name: String): ScheduledAppointmentEstimate {
    return ScheduledAppointmentEstimate(
        code = code,
        name = name,
        lane = "",
        startsAt = 0,
        locale = "",
        vehicleId = "",
        status = ScheduledAppointmentEstimateStatus.DRAFT,
    )
}
