package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive cutoff variance is in its life cycle. */
enum class AdaptiveCutoffVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One adaptive cutoff variance, with what the planners need to act on it. */
data class AdaptiveCutoffVariance(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var country: String,
    var currency: String,
    var fuelLiters: Double,
    var status: AdaptiveCutoffVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCutoffVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCutoffVarianceStatus): Boolean {
        when (status) {
            AdaptiveCutoffVarianceStatus.PLANNED -> return next == AdaptiveCutoffVarianceStatus.IN_PROGRESS
            AdaptiveCutoffVarianceStatus.SCHEDULED -> return next == AdaptiveCutoffVarianceStatus.IN_PROGRESS || next == AdaptiveCutoffVarianceStatus.CANCELLED
            AdaptiveCutoffVarianceStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveCutoffVarianceStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveCutoffVarianceStatus.COMPLETED -> return next == AdaptiveCutoffVarianceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AdaptiveCutoffVarianceStatus) {
        if (!canMoveTo(next)) {
            error("adaptive cutoff variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveValue(): Double = round((etaMinutes.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): AdaptiveCutoffVariance {
        return AdaptiveCutoffVariance(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            country = country,
            currency = currency,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive cutoff variance in status planned. */
fun newAdaptiveCutoffVariance(code: String, title: String): AdaptiveCutoffVariance {
    return AdaptiveCutoffVariance(
        code = code,
        title = title,
        etaMinutes = 0,
        country = "",
        currency = "",
        fuelLiters = 0.0,
        status = AdaptiveCutoffVarianceStatus.PLANNED,
    )
}
