package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted proof of delivery review is in its life cycle. */
enum class WeightedProofOfDeliveryReviewStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted proof of delivery review. Its code is unique within a tenant. */
data class WeightedProofOfDeliveryReview(
    var code: String,
    var label: String,
    var odometer: Int,
    var durationMinutes: Int,
    var loadKg: Double,
    var unit: String,
    var notes: String,
    var quantity: Int,
    var status: WeightedProofOfDeliveryReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedProofOfDeliveryReviewStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedProofOfDeliveryReviewStatus): Boolean {
        when (status) {
            WeightedProofOfDeliveryReviewStatus.DRAFT -> {
                when (next) {
                    WeightedProofOfDeliveryReviewStatus.PENDING -> return true
                }
                return false
            }
            WeightedProofOfDeliveryReviewStatus.PENDING -> {
                when (next) {
                    WeightedProofOfDeliveryReviewStatus.SUSPENDED -> return true
                }
                return false
            }
            WeightedProofOfDeliveryReviewStatus.ACTIVE -> return next == WeightedProofOfDeliveryReviewStatus.SUSPENDED || next == WeightedProofOfDeliveryReviewStatus.CLOSED
            WeightedProofOfDeliveryReviewStatus.SUSPENDED -> {
                when (next) {
                    WeightedProofOfDeliveryReviewStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedProofOfDeliveryReviewStatus) {
        if (!canMoveTo(next)) {
            error("weighted proof of delivery review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedLoad(): Double = round((quantity.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): WeightedProofOfDeliveryReview {
        return WeightedProofOfDeliveryReview(
            code = code,
            label = label,
            odometer = odometer,
            durationMinutes = durationMinutes,
            loadKg = loadKg,
            unit = unit,
            notes = notes,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a weighted proof of delivery review in status draft. */
fun newWeightedProofOfDeliveryReview(code: String, label: String): WeightedProofOfDeliveryReview {
    return WeightedProofOfDeliveryReview(
        code = code,
        label = label,
        odometer = 0,
        durationMinutes = 0,
        loadKg = 0.0,
        unit = "",
        notes = "",
        quantity = 0,
        status = WeightedProofOfDeliveryReviewStatus.DRAFT,
    )
}
