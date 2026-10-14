package com.acme.transport.lastmile.proofofdelivery.deliveryrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted proof of delivery override is in its life cycle. */
enum class WeightedProofOfDeliveryOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted proof of delivery override. Its code is unique within a tenant. */
data class WeightedProofOfDeliveryOverride(
    var code: String,
    var title: String,
    var summary: String,
    var ratio: Double,
    var stopCount: Int,
    var capacity: Int,
    var locale: String,
    var status: WeightedProofOfDeliveryOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedProofOfDeliveryOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedProofOfDeliveryOverrideStatus): Boolean {
        when (status) {
            WeightedProofOfDeliveryOverrideStatus.OPEN -> return next == WeightedProofOfDeliveryOverrideStatus.IN_REVIEW || next == WeightedProofOfDeliveryOverrideStatus.RESOLVED
            WeightedProofOfDeliveryOverrideStatus.IN_REVIEW -> return next == WeightedProofOfDeliveryOverrideStatus.RESOLVED || next == WeightedProofOfDeliveryOverrideStatus.REOPENED
            WeightedProofOfDeliveryOverrideStatus.RESOLVED -> return next == WeightedProofOfDeliveryOverrideStatus.REOPENED || next == WeightedProofOfDeliveryOverrideStatus.ARCHIVED
            WeightedProofOfDeliveryOverrideStatus.REOPENED -> {
                when (next) {
                    WeightedProofOfDeliveryOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedProofOfDeliveryOverrideStatus) {
        if (!canMoveTo(next)) {
            error("weighted proof of delivery override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedTotal(): Double = round((stopCount.toDouble() - ratio) * 100) / 100

    fun snapshot(): WeightedProofOfDeliveryOverride {
        return WeightedProofOfDeliveryOverride(
            code = code,
            title = title,
            summary = summary,
            ratio = ratio,
            stopCount = stopCount,
            capacity = capacity,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a weighted proof of delivery override in status open. */
fun newWeightedProofOfDeliveryOverride(code: String, title: String): WeightedProofOfDeliveryOverride {
    return WeightedProofOfDeliveryOverride(
        code = code,
        title = title,
        summary = "",
        ratio = 0.0,
        stopCount = 0,
        capacity = 0,
        locale = "",
        status = WeightedProofOfDeliveryOverrideStatus.OPEN,
    )
}
