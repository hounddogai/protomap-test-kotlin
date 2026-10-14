package com.acme.transport.emissions.evcharge.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy ev charge variance is in its life cycle. */
enum class LegacyEvChargeVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a legacy ev charge variance and its current status. */
data class LegacyEvChargeVariance(
    var code: String,
    var title: String,
    var channel: String,
    var stopCount: Int,
    var threshold: Double,
    var region: String,
    var pinned: Boolean,
    var retries: Int,
    var currency: String,
    var status: LegacyEvChargeVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyEvChargeVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyEvChargeVarianceStatus): Boolean {
        when (status) {
            LegacyEvChargeVarianceStatus.CREATED -> return next == LegacyEvChargeVarianceStatus.VALIDATED
            LegacyEvChargeVarianceStatus.VALIDATED -> {
                when (next) {
                    LegacyEvChargeVarianceStatus.RETIRED -> return true
                }
                return false
            }
            LegacyEvChargeVarianceStatus.PUBLISHED -> {
                when (next) {
                    LegacyEvChargeVarianceStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyEvChargeVarianceStatus) {
        if (!canMoveTo(next)) {
            error("legacy ev charge variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy ev charge variance"} $code"

    fun weightedVolume(): Double = round((retries.toDouble() + threshold) * 100) / 100

    fun snapshot(): LegacyEvChargeVariance {
        return LegacyEvChargeVariance(
            code = code,
            title = title,
            channel = channel,
            stopCount = stopCount,
            threshold = threshold,
            region = region,
            pinned = pinned,
            retries = retries,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy ev charge variance in status created. */
fun newLegacyEvChargeVariance(code: String, title: String): LegacyEvChargeVariance {
    return LegacyEvChargeVariance(
        code = code,
        title = title,
        channel = "",
        stopCount = 0,
        threshold = 0.0,
        region = "",
        pinned = false,
        retries = 0,
        currency = "",
        status = LegacyEvChargeVarianceStatus.CREATED,
    )
}
