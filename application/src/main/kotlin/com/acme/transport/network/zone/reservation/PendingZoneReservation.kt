package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.text.normalize

// The pending zone reservation records of network's zone work, and the rules, stores, and calculations around
// them.

/** Where a pending zone reservation is in its life cycle. */
enum class PendingZoneReservationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A pending zone reservation as the zone team keeps it. */
data class PendingZoneReservation(
    var code: String,
    var label: String,
    var archived: Boolean,
    var channel: String,
    var attempts: Int,
    var status: PendingZoneReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingZoneReservationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingZoneReservationStatus): Boolean {
        when (status) {
            PendingZoneReservationStatus.PROPOSED -> return next == PendingZoneReservationStatus.APPROVED || next == PendingZoneReservationStatus.REJECTED
            PendingZoneReservationStatus.APPROVED -> {
                when (next) {
                    PendingZoneReservationStatus.REJECTED -> return true
                }
                return false
            }
            PendingZoneReservationStatus.REJECTED -> {
                when (next) {
                    PendingZoneReservationStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingZoneReservationStatus) {
        if (!canMoveTo(next)) {
            error("pending zone reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending zone reservation"} $code"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): PendingZoneReservation {
        return PendingZoneReservation(
            code = code,
            label = label,
            archived = archived,
            channel = channel,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending zone reservation in status proposed. */
fun newPendingZoneReservation(code: String, label: String): PendingZoneReservation {
    return PendingZoneReservation(
        code = code,
        label = label,
        archived = false,
        channel = "",
        attempts = 0,
        status = PendingZoneReservationStatus.PROPOSED,
    )
}
