package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.text.normalize

/** Where a pending backhaul reservation is in its life cycle. */
enum class PendingBackhaulReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a pending backhaul reservation and its current status. */
data class PendingBackhaulReservation(
    var code: String,
    var name: String,
    var approved: Boolean,
    var phone: String,
    var driverId: String,
    var ownerId: String,
    var tags: MutableList<String>,
    var quantity: Int,
    var status: PendingBackhaulReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingBackhaulReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingBackhaulReservationStatus): Boolean {
        when (status) {
            PendingBackhaulReservationStatus.IDLE -> return next == PendingBackhaulReservationStatus.STOPPED
            PendingBackhaulReservationStatus.WARMING -> {
                when (next) {
                    PendingBackhaulReservationStatus.READY, PendingBackhaulReservationStatus.STOPPED -> return true
                }
                return false
            }
            PendingBackhaulReservationStatus.READY -> {
                when (next) {
                    PendingBackhaulReservationStatus.DRAINING -> return true
                }
                return false
            }
            PendingBackhaulReservationStatus.DRAINING -> {
                when (next) {
                    PendingBackhaulReservationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingBackhaulReservationStatus) {
        if (!canMoveTo(next)) {
            error("pending backhaul reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending backhaul reservation"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): PendingBackhaulReservation {
        return PendingBackhaulReservation(
            code = code,
            name = name,
            approved = approved,
            phone = phone,
            driverId = driverId,
            ownerId = ownerId,
            tags = tags,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending backhaul reservation in status idle. */
fun newPendingBackhaulReservation(code: String, name: String): PendingBackhaulReservation {
    return PendingBackhaulReservation(
        code = code,
        name = name,
        approved = false,
        phone = "",
        driverId = "",
        ownerId = "",
        tags = mutableListOf<String>(),
        quantity = 0,
        status = PendingBackhaulReservationStatus.IDLE,
    )
}
