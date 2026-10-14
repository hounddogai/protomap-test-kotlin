package com.acme.transport.terminal.appointment.ledger

import com.acme.transport.kit.text.normalize

/** Where a seasonal appointment override is in its life cycle. */
enum class SeasonalAppointmentOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal appointment override as the appointment team keeps it. */
data class SeasonalAppointmentOverride(
    var code: String,
    var label: String,
    var trailerId: String,
    var ownerId: String,
    var dock: String,
    var currency: String,
    var amount: Double,
    var status: SeasonalAppointmentOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAppointmentOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAppointmentOverrideStatus): Boolean {
        when (status) {
            SeasonalAppointmentOverrideStatus.IDLE -> {
                when (next) {
                    SeasonalAppointmentOverrideStatus.WARMING -> return true
                }
                return false
            }
            SeasonalAppointmentOverrideStatus.WARMING -> return next == SeasonalAppointmentOverrideStatus.DRAINING || next == SeasonalAppointmentOverrideStatus.STOPPED
            SeasonalAppointmentOverrideStatus.READY -> {
                when (next) {
                    SeasonalAppointmentOverrideStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalAppointmentOverrideStatus.DRAINING -> return next == SeasonalAppointmentOverrideStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SeasonalAppointmentOverrideStatus) {
        if (!canMoveTo(next)) {
            error("seasonal appointment override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = amount

    fun snapshot(): SeasonalAppointmentOverride {
        return SeasonalAppointmentOverride(
            code = code,
            label = label,
            trailerId = trailerId,
            ownerId = ownerId,
            dock = dock,
            currency = currency,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal appointment override in status idle. */
fun newSeasonalAppointmentOverride(code: String, label: String): SeasonalAppointmentOverride {
    return SeasonalAppointmentOverride(
        code = code,
        label = label,
        trailerId = "",
        ownerId = "",
        dock = "",
        currency = "",
        amount = 0.0,
        status = SeasonalAppointmentOverrideStatus.IDLE,
    )
}
