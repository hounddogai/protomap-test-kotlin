package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive near miss variance records of safety's near miss work, and the rules, stores, and calculations
// around them.

/** Where a adaptive near miss variance is in its life cycle. */
enum class AdaptiveNearMissVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One adaptive near miss variance, with what the planners need to act on it. */
data class AdaptiveNearMissVariance(
    var code: String,
    var label: String,
    var pallets: Int,
    var ratio: Double,
    var ownerId: String,
    var status: AdaptiveNearMissVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveNearMissVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveNearMissVarianceStatus): Boolean {
        when (status) {
            AdaptiveNearMissVarianceStatus.OPEN -> {
                when (next) {
                    AdaptiveNearMissVarianceStatus.RESOLVED -> return true
                }
                return false
            }
            AdaptiveNearMissVarianceStatus.IN_REVIEW -> {
                when (next) {
                    AdaptiveNearMissVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
            AdaptiveNearMissVarianceStatus.RESOLVED -> {
                when (next) {
                    AdaptiveNearMissVarianceStatus.REOPENED, AdaptiveNearMissVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
            AdaptiveNearMissVarianceStatus.REOPENED -> return next == AdaptiveNearMissVarianceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AdaptiveNearMissVarianceStatus) {
        if (!canMoveTo(next)) {
            error("adaptive near miss variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive near miss variance"} $code"

    fun effectiveExposure(): Double = round((ratio + pallets.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveNearMissVariance {
        return AdaptiveNearMissVariance(
            code = code,
            label = label,
            pallets = pallets,
            ratio = ratio,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive near miss variance in status open. */
fun newAdaptiveNearMissVariance(code: String, label: String): AdaptiveNearMissVariance {
    return AdaptiveNearMissVariance(
        code = code,
        label = label,
        pallets = 0,
        ratio = 0.0,
        ownerId = "",
        status = AdaptiveNearMissVarianceStatus.OPEN,
    )
}
