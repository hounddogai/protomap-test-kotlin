package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.text.normalize

/** Where a strict proof of delivery reservation is in its life cycle. */
enum class StrictProofOfDeliveryReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One strict proof of delivery reservation, with what the planners need to act on it. */
data class StrictProofOfDeliveryReservation(
    var code: String,
    var label: String,
    var name: String,
    var region: String,
    var approved: Boolean,
    var loadKg: Double,
    var trailerId: String,
    var attributes: MutableMap<String, String>,
    var status: StrictProofOfDeliveryReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictProofOfDeliveryReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictProofOfDeliveryReservationStatus): Boolean {
        when (status) {
            StrictProofOfDeliveryReservationStatus.DRAFT -> return next == StrictProofOfDeliveryReservationStatus.SUSPENDED || next == StrictProofOfDeliveryReservationStatus.CLOSED
            StrictProofOfDeliveryReservationStatus.PENDING -> {
                when (next) {
                    StrictProofOfDeliveryReservationStatus.ACTIVE -> return true
                }
                return false
            }
            StrictProofOfDeliveryReservationStatus.ACTIVE -> return next == StrictProofOfDeliveryReservationStatus.SUSPENDED || next == StrictProofOfDeliveryReservationStatus.CLOSED
            StrictProofOfDeliveryReservationStatus.SUSPENDED -> {
                when (next) {
                    StrictProofOfDeliveryReservationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictProofOfDeliveryReservationStatus) {
        if (!canMoveTo(next)) {
            error("strict proof of delivery reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = loadKg

    fun snapshot(): StrictProofOfDeliveryReservation {
        return StrictProofOfDeliveryReservation(
            code = code,
            label = label,
            name = name,
            region = region,
            approved = approved,
            loadKg = loadKg,
            trailerId = trailerId,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a strict proof of delivery reservation in status draft. */
fun newStrictProofOfDeliveryReservation(code: String, label: String): StrictProofOfDeliveryReservation {
    return StrictProofOfDeliveryReservation(
        code = code,
        label = label,
        name = "",
        region = "",
        approved = false,
        loadKg = 0.0,
        trailerId = "",
        attributes = mutableMapOf<String, String>(),
        status = StrictProofOfDeliveryReservationStatus.DRAFT,
    )
}
