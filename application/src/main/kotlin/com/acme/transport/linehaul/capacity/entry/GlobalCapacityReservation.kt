package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.kit.text.normalize

/** Where a global capacity reservation is in its life cycle. */
enum class GlobalCapacityReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a global capacity reservation and its current status. */
data class GlobalCapacityReservation(
    var code: String,
    var name: String,
    var attempts: Int,
    var region: String,
    var approved: Boolean,
    var status: GlobalCapacityReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCapacityReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCapacityReservationStatus): Boolean {
        when (status) {
            GlobalCapacityReservationStatus.PLANNED -> {
                when (next) {
                    GlobalCapacityReservationStatus.SCHEDULED -> return true
                }
                return false
            }
            GlobalCapacityReservationStatus.SCHEDULED -> return next == GlobalCapacityReservationStatus.IN_PROGRESS || next == GlobalCapacityReservationStatus.COMPLETED
            GlobalCapacityReservationStatus.IN_PROGRESS -> return next == GlobalCapacityReservationStatus.CANCELLED
            GlobalCapacityReservationStatus.COMPLETED -> return next == GlobalCapacityReservationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: GlobalCapacityReservationStatus) {
        if (!canMoveTo(next)) {
            error("global capacity reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): GlobalCapacityReservation {
        return GlobalCapacityReservation(
            code = code,
            name = name,
            attempts = attempts,
            region = region,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global capacity reservation in status planned. */
fun newGlobalCapacityReservation(code: String, name: String): GlobalCapacityReservation {
    return GlobalCapacityReservation(
        code = code,
        name = name,
        attempts = 0,
        region = "",
        approved = false,
        status = GlobalCapacityReservationStatus.PLANNED,
    )
}
