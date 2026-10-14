package com.acme.transport.lastmile.proofofdelivery.segment

import com.acme.transport.kit.text.normalize

// The weighted proof of delivery segment records of lastmile's proof of delivery work, and the rules, stores,
// and calculations around them.

/** Where a weighted proof of delivery segment is in its life cycle. */
enum class WeightedProofOfDeliverySegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted proof of delivery segment. Its code is unique within a tenant. */
data class WeightedProofOfDeliverySegment(
    var code: String,
    var name: String,
    var priority: Int,
    var dock: String,
    var shiftCode: String,
    var title: String,
    var lane: String,
    var status: WeightedProofOfDeliverySegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedProofOfDeliverySegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedProofOfDeliverySegmentStatus): Boolean {
        when (status) {
            WeightedProofOfDeliverySegmentStatus.PLANNED -> return next == WeightedProofOfDeliverySegmentStatus.CANCELLED
            WeightedProofOfDeliverySegmentStatus.SCHEDULED -> {
                when (next) {
                    WeightedProofOfDeliverySegmentStatus.IN_PROGRESS, WeightedProofOfDeliverySegmentStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedProofOfDeliverySegmentStatus.IN_PROGRESS -> return next == WeightedProofOfDeliverySegmentStatus.COMPLETED
            WeightedProofOfDeliverySegmentStatus.COMPLETED -> {
                when (next) {
                    WeightedProofOfDeliverySegmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedProofOfDeliverySegmentStatus) {
        if (!canMoveTo(next)) {
            error("weighted proof of delivery segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): WeightedProofOfDeliverySegment {
        return WeightedProofOfDeliverySegment(
            code = code,
            name = name,
            priority = priority,
            dock = dock,
            shiftCode = shiftCode,
            title = title,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted proof of delivery segment in status planned. */
fun newWeightedProofOfDeliverySegment(code: String, name: String): WeightedProofOfDeliverySegment {
    return WeightedProofOfDeliverySegment(
        code = code,
        name = name,
        priority = 0,
        dock = "",
        shiftCode = "",
        title = "",
        lane = "",
        status = WeightedProofOfDeliverySegmentStatus.PLANNED,
    )
}
