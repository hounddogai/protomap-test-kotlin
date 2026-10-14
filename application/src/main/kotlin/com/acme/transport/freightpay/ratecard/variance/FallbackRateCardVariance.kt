package com.acme.transport.freightpay.ratecard.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback rate card variance records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a fallback rate card variance is in its life cycle. */
enum class FallbackRateCardVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback rate card variance. Its code is unique within a tenant. */
data class FallbackRateCardVariance(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var graceDays: Int,
    var startsAt: Int,
    var retries: Int,
    var status: FallbackRateCardVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRateCardVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRateCardVarianceStatus): Boolean {
        when (status) {
            FallbackRateCardVarianceStatus.IDLE -> {
                when (next) {
                    FallbackRateCardVarianceStatus.READY, FallbackRateCardVarianceStatus.DRAINING -> return true
                }
                return false
            }
            FallbackRateCardVarianceStatus.WARMING -> return next == FallbackRateCardVarianceStatus.DRAINING
            FallbackRateCardVarianceStatus.READY -> return next == FallbackRateCardVarianceStatus.DRAINING || next == FallbackRateCardVarianceStatus.STOPPED
            FallbackRateCardVarianceStatus.DRAINING -> return next == FallbackRateCardVarianceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FallbackRateCardVarianceStatus) {
        if (!canMoveTo(next)) {
            error("fallback rate card variance $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedVolume(): Double = round((startsAt.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): FallbackRateCardVariance {
        return FallbackRateCardVariance(
            code = code,
            title = title,
            distanceKm = distanceKm,
            graceDays = graceDays,
            startsAt = startsAt,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback rate card variance in status idle. */
fun newFallbackRateCardVariance(code: String, title: String): FallbackRateCardVariance {
    return FallbackRateCardVariance(
        code = code,
        title = title,
        distanceKm = 0.0,
        graceDays = 0,
        startsAt = 0,
        retries = 0,
        status = FallbackRateCardVarianceStatus.IDLE,
    )
}
