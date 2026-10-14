package com.acme.transport.lastmile.proofofdelivery.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The proof of delivery reservation records of lastmile's proof of delivery work, and the rules, stores, and
// calculations around them.

/** Where a proof of delivery reservation is in its life cycle. */
enum class ProofOfDeliveryReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One proof of delivery reservation, with what the planners need to act on it. */
data class ProofOfDeliveryReservation(
    var code: String,
    var title: String,
    var limit: Int,
    var source: String,
    var durationMinutes: Int,
    var quantity: Int,
    var trailerId: String,
    var status: ProofOfDeliveryReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryReservationStatus): Boolean {
        when (status) {
            ProofOfDeliveryReservationStatus.PLANNED -> {
                when (next) {
                    ProofOfDeliveryReservationStatus.IN_PROGRESS, ProofOfDeliveryReservationStatus.CANCELLED -> return true
                }
                return false
            }
            ProofOfDeliveryReservationStatus.SCHEDULED -> return next == ProofOfDeliveryReservationStatus.IN_PROGRESS
            ProofOfDeliveryReservationStatus.IN_PROGRESS -> return next == ProofOfDeliveryReservationStatus.COMPLETED || next == ProofOfDeliveryReservationStatus.CANCELLED
            ProofOfDeliveryReservationStatus.COMPLETED -> {
                when (next) {
                    ProofOfDeliveryReservationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryReservationStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"proof of delivery reservation"} $code"

    fun adjustedValue(): Double = round(durationMinutes.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): ProofOfDeliveryReservation {
        return ProofOfDeliveryReservation(
            code = code,
            title = title,
            limit = limit,
            source = source,
            durationMinutes = durationMinutes,
            quantity = quantity,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a proof of delivery reservation in status planned. */
fun newProofOfDeliveryReservation(code: String, title: String): ProofOfDeliveryReservation {
    return ProofOfDeliveryReservation(
        code = code,
        title = title,
        limit = 0,
        source = "",
        durationMinutes = 0,
        quantity = 0,
        trailerId = "",
        status = ProofOfDeliveryReservationStatus.PLANNED,
    )
}
