package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual fuel burn variance is in its life cycle. */
enum class ManualFuelBurnVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One manual fuel burn variance, with what the planners need to act on it. */
data class ManualFuelBurnVariance(
    var code: String,
    var title: String,
    var score: Double,
    var endsAt: Int,
    var channel: String,
    var source: String,
    var lane: String,
    var status: ManualFuelBurnVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualFuelBurnVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualFuelBurnVarianceStatus): Boolean {
        when (status) {
            ManualFuelBurnVarianceStatus.PROPOSED -> {
                when (next) {
                    ManualFuelBurnVarianceStatus.APPROVED, ManualFuelBurnVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualFuelBurnVarianceStatus.APPROVED -> return next == ManualFuelBurnVarianceStatus.WITHDRAWN
            ManualFuelBurnVarianceStatus.REJECTED -> {
                when (next) {
                    ManualFuelBurnVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualFuelBurnVarianceStatus) {
        if (!canMoveTo(next)) {
            error("manual fuel burn variance $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netValue(): Double = round(score * endsAt.toDouble() * 100) / 100

    fun snapshot(): ManualFuelBurnVariance {
        return ManualFuelBurnVariance(
            code = code,
            title = title,
            score = score,
            endsAt = endsAt,
            channel = channel,
            source = source,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a manual fuel burn variance in status proposed. */
fun newManualFuelBurnVariance(code: String, title: String): ManualFuelBurnVariance {
    return ManualFuelBurnVariance(
        code = code,
        title = title,
        score = 0.0,
        endsAt = 0,
        channel = "",
        source = "",
        lane = "",
        status = ManualFuelBurnVarianceStatus.PROPOSED,
    )
}
