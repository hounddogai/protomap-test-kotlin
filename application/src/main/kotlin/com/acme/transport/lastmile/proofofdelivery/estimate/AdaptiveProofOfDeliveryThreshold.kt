package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive proof of delivery threshold is in its life cycle. */
enum class AdaptiveProofOfDeliveryThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One adaptive proof of delivery threshold, with what the planners need to act on it. */
data class AdaptiveProofOfDeliveryThreshold(
    var code: String,
    var name: String,
    var externalRef: String,
    var ceiling: Double,
    var description: String,
    var routeCode: String,
    var weight: Double,
    var title: String,
    var status: AdaptiveProofOfDeliveryThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveProofOfDeliveryThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveProofOfDeliveryThresholdStatus): Boolean {
        when (status) {
            AdaptiveProofOfDeliveryThresholdStatus.OPEN -> {
                when (next) {
                    AdaptiveProofOfDeliveryThresholdStatus.REOPENED, AdaptiveProofOfDeliveryThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
            AdaptiveProofOfDeliveryThresholdStatus.IN_REVIEW -> {
                when (next) {
                    AdaptiveProofOfDeliveryThresholdStatus.RESOLVED, AdaptiveProofOfDeliveryThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
            AdaptiveProofOfDeliveryThresholdStatus.RESOLVED -> return next == AdaptiveProofOfDeliveryThresholdStatus.REOPENED || next == AdaptiveProofOfDeliveryThresholdStatus.ARCHIVED
            AdaptiveProofOfDeliveryThresholdStatus.REOPENED -> return next == AdaptiveProofOfDeliveryThresholdStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AdaptiveProofOfDeliveryThresholdStatus) {
        if (!canMoveTo(next)) {
            error("adaptive proof of delivery threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive proof of delivery threshold"} $code"

    fun effectiveValue(): Double = round((ceiling + weight) * 100) / 100

    fun snapshot(): AdaptiveProofOfDeliveryThreshold {
        return AdaptiveProofOfDeliveryThreshold(
            code = code,
            name = name,
            externalRef = externalRef,
            ceiling = ceiling,
            description = description,
            routeCode = routeCode,
            weight = weight,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive proof of delivery threshold in status open. */
fun newAdaptiveProofOfDeliveryThreshold(code: String, name: String): AdaptiveProofOfDeliveryThreshold {
    return AdaptiveProofOfDeliveryThreshold(
        code = code,
        name = name,
        externalRef = "",
        ceiling = 0.0,
        description = "",
        routeCode = "",
        weight = 0.0,
        title = "",
        status = AdaptiveProofOfDeliveryThresholdStatus.OPEN,
    )
}
