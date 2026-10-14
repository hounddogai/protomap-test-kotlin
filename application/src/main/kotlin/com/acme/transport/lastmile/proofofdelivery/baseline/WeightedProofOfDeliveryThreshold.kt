package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted proof of delivery threshold is in its life cycle. */
enum class WeightedProofOfDeliveryThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A weighted proof of delivery threshold. Its code is unique within a tenant. */
data class WeightedProofOfDeliveryThreshold(
    var code: String,
    var title: String,
    var approved: Boolean,
    var updatedAt: Int,
    var archived: Boolean,
    var sequence: Int,
    var ownerId: String,
    var unit: String,
    var weight: Double,
    var status: WeightedProofOfDeliveryThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedProofOfDeliveryThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedProofOfDeliveryThresholdStatus): Boolean {
        when (status) {
            WeightedProofOfDeliveryThresholdStatus.PROPOSED -> {
                when (next) {
                    WeightedProofOfDeliveryThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedProofOfDeliveryThresholdStatus.APPROVED -> {
                when (next) {
                    WeightedProofOfDeliveryThresholdStatus.REJECTED, WeightedProofOfDeliveryThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedProofOfDeliveryThresholdStatus.REJECTED -> return next == WeightedProofOfDeliveryThresholdStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: WeightedProofOfDeliveryThresholdStatus) {
        if (!canMoveTo(next)) {
            error("weighted proof of delivery threshold $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun weightedVolume(): Double = round((sequence.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedProofOfDeliveryThreshold {
        return WeightedProofOfDeliveryThreshold(
            code = code,
            title = title,
            approved = approved,
            updatedAt = updatedAt,
            archived = archived,
            sequence = sequence,
            ownerId = ownerId,
            unit = unit,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a weighted proof of delivery threshold in status proposed. */
fun newWeightedProofOfDeliveryThreshold(code: String, title: String): WeightedProofOfDeliveryThreshold {
    return WeightedProofOfDeliveryThreshold(
        code = code,
        title = title,
        approved = false,
        updatedAt = 0,
        archived = false,
        sequence = 0,
        ownerId = "",
        unit = "",
        weight = 0.0,
        status = WeightedProofOfDeliveryThresholdStatus.PROPOSED,
    )
}
