package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive fuel burn estimate records of emissions's fuel burn work, and the rules, stores, and
// calculations around them.

/** Where a adaptive fuel burn estimate is in its life cycle. */
enum class AdaptiveFuelBurnEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One adaptive fuel burn estimate, with what the planners need to act on it. */
data class AdaptiveFuelBurnEstimate(
    var code: String,
    var title: String,
    var discount: Double,
    var source: String,
    var ratio: Double,
    var status: AdaptiveFuelBurnEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveFuelBurnEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveFuelBurnEstimateStatus): Boolean {
        when (status) {
            AdaptiveFuelBurnEstimateStatus.CREATED -> return next == AdaptiveFuelBurnEstimateStatus.PUBLISHED
            AdaptiveFuelBurnEstimateStatus.VALIDATED -> {
                when (next) {
                    AdaptiveFuelBurnEstimateStatus.PUBLISHED -> return true
                }
                return false
            }
            AdaptiveFuelBurnEstimateStatus.PUBLISHED -> return next == AdaptiveFuelBurnEstimateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveFuelBurnEstimateStatus) {
        if (!canMoveTo(next)) {
            error("adaptive fuel burn estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive fuel burn estimate"} $code"

    fun effectiveVolume(): Double = round((discount - ratio) * 100) / 100

    fun snapshot(): AdaptiveFuelBurnEstimate {
        return AdaptiveFuelBurnEstimate(
            code = code,
            title = title,
            discount = discount,
            source = source,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a adaptive fuel burn estimate in status created. */
fun newAdaptiveFuelBurnEstimate(code: String, title: String): AdaptiveFuelBurnEstimate {
    return AdaptiveFuelBurnEstimate(
        code = code,
        title = title,
        discount = 0.0,
        source = "",
        ratio = 0.0,
        status = AdaptiveFuelBurnEstimateStatus.CREATED,
    )
}
