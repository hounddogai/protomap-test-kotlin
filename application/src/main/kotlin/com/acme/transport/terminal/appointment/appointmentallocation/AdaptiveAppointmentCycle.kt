package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.text.normalize

/** Where a adaptive appointment cycle is in its life cycle. */
enum class AdaptiveAppointmentCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a adaptive appointment cycle and its current status. */
data class AdaptiveAppointmentCycle(
    var code: String,
    var label: String,
    var vehicleId: String,
    var visible: Boolean,
    var notes: String,
    var country: String,
    var startsAt: Int,
    var archived: Boolean,
    var status: AdaptiveAppointmentCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAppointmentCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAppointmentCycleStatus): Boolean {
        when (status) {
            AdaptiveAppointmentCycleStatus.PLANNED -> return next == AdaptiveAppointmentCycleStatus.SCHEDULED || next == AdaptiveAppointmentCycleStatus.COMPLETED
            AdaptiveAppointmentCycleStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveAppointmentCycleStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveAppointmentCycleStatus.IN_PROGRESS -> return next == AdaptiveAppointmentCycleStatus.COMPLETED
            AdaptiveAppointmentCycleStatus.COMPLETED -> {
                when (next) {
                    AdaptiveAppointmentCycleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveAppointmentCycleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive appointment cycle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"adaptive appointment cycle"} $code"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): AdaptiveAppointmentCycle {
        return AdaptiveAppointmentCycle(
            code = code,
            label = label,
            vehicleId = vehicleId,
            visible = visible,
            notes = notes,
            country = country,
            startsAt = startsAt,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive appointment cycle in status planned. */
fun newAdaptiveAppointmentCycle(code: String, label: String): AdaptiveAppointmentCycle {
    return AdaptiveAppointmentCycle(
        code = code,
        label = label,
        vehicleId = "",
        visible = false,
        notes = "",
        country = "",
        startsAt = 0,
        archived = false,
        status = AdaptiveAppointmentCycleStatus.PLANNED,
    )
}
