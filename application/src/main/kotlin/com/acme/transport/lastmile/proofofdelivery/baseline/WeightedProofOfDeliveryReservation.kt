package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted proof of delivery reservation is in its life cycle. */
enum class WeightedProofOfDeliveryReservationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted proof of delivery reservation as the proof of delivery team keeps it. */
data class WeightedProofOfDeliveryReservation(
    var code: String,
    var label: String,
    var notes: String,
    var capacity: Int,
    var limit: Int,
    var lane: String,
    var status: WeightedProofOfDeliveryReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedProofOfDeliveryReservationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedProofOfDeliveryReservationStatus): Boolean {
        when (status) {
            WeightedProofOfDeliveryReservationStatus.REQUESTED -> {
                when (next) {
                    WeightedProofOfDeliveryReservationStatus.GRANTED, WeightedProofOfDeliveryReservationStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedProofOfDeliveryReservationStatus.GRANTED -> return next == WeightedProofOfDeliveryReservationStatus.REVOKED || next == WeightedProofOfDeliveryReservationStatus.EXPIRED
            WeightedProofOfDeliveryReservationStatus.REVOKED -> {
                when (next) {
                    WeightedProofOfDeliveryReservationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedProofOfDeliveryReservationStatus) {
        if (!canMoveTo(next)) {
            error("weighted proof of delivery reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedTotal(): Double = round((limit.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): WeightedProofOfDeliveryReservation {
        return WeightedProofOfDeliveryReservation(
            code = code,
            label = label,
            notes = notes,
            capacity = capacity,
            limit = limit,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted proof of delivery reservation in status requested. */
fun newWeightedProofOfDeliveryReservation(code: String, label: String): WeightedProofOfDeliveryReservation {
    return WeightedProofOfDeliveryReservation(
        code = code,
        label = label,
        notes = "",
        capacity = 0,
        limit = 0,
        lane = "",
        status = WeightedProofOfDeliveryReservationStatus.REQUESTED,
    )
}
