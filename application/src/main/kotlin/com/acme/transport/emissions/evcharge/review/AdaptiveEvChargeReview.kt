package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive ev charge review records of emissions's ev charge work, and the rules, stores, and
// calculations around them.

/** Where a adaptive ev charge review is in its life cycle. */
enum class AdaptiveEvChargeReviewStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One adaptive ev charge review, with what the planners need to act on it. */
data class AdaptiveEvChargeReview(
    var code: String,
    var label: String,
    var active: Boolean,
    var loadKg: Double,
    var segment: String,
    var retries: Int,
    var endsAt: Int,
    var status: AdaptiveEvChargeReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveEvChargeReviewStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveEvChargeReviewStatus): Boolean {
        when (status) {
            AdaptiveEvChargeReviewStatus.PLANNED -> return next == AdaptiveEvChargeReviewStatus.SCHEDULED || next == AdaptiveEvChargeReviewStatus.CANCELLED
            AdaptiveEvChargeReviewStatus.SCHEDULED -> return next == AdaptiveEvChargeReviewStatus.COMPLETED || next == AdaptiveEvChargeReviewStatus.CANCELLED
            AdaptiveEvChargeReviewStatus.IN_PROGRESS -> return next == AdaptiveEvChargeReviewStatus.COMPLETED
            AdaptiveEvChargeReviewStatus.COMPLETED -> {
                when (next) {
                    AdaptiveEvChargeReviewStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveEvChargeReviewStatus) {
        if (!canMoveTo(next)) {
            error("adaptive ev charge review $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedExposure(): Double = round((loadKg + retries.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveEvChargeReview {
        return AdaptiveEvChargeReview(
            code = code,
            label = label,
            active = active,
            loadKg = loadKg,
            segment = segment,
            retries = retries,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a adaptive ev charge review in status planned. */
fun newAdaptiveEvChargeReview(code: String, label: String): AdaptiveEvChargeReview {
    return AdaptiveEvChargeReview(
        code = code,
        label = label,
        active = false,
        loadKg = 0.0,
        segment = "",
        retries = 0,
        endsAt = 0,
        status = AdaptiveEvChargeReviewStatus.PLANNED,
    )
}
