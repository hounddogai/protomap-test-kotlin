package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.text.normalize

/** Where a global dispute reservation is in its life cycle. */
enum class GlobalDisputeReservationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a global dispute reservation and its current status. */
data class GlobalDisputeReservation(
    var code: String,
    var name: String,
    var retries: Int,
    var region: String,
    var ownerId: String,
    var status: GlobalDisputeReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDisputeReservationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDisputeReservationStatus): Boolean {
        when (status) {
            GlobalDisputeReservationStatus.REQUESTED -> return next == GlobalDisputeReservationStatus.EXPIRED
            GlobalDisputeReservationStatus.GRANTED -> {
                when (next) {
                    GlobalDisputeReservationStatus.REVOKED, GlobalDisputeReservationStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalDisputeReservationStatus.REVOKED -> {
                when (next) {
                    GlobalDisputeReservationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalDisputeReservationStatus) {
        if (!canMoveTo(next)) {
            error("global dispute reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): GlobalDisputeReservation {
        return GlobalDisputeReservation(
            code = code,
            name = name,
            retries = retries,
            region = region,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global dispute reservation in status requested. */
fun newGlobalDisputeReservation(code: String, name: String): GlobalDisputeReservation {
    return GlobalDisputeReservation(
        code = code,
        name = name,
        retries = 0,
        region = "",
        ownerId = "",
        status = GlobalDisputeReservationStatus.REQUESTED,
    )
}
