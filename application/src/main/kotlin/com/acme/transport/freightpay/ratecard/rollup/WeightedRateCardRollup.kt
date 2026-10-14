package com.acme.transport.freightpay.ratecard.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted rate card rollup records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a weighted rate card rollup is in its life cycle. */
enum class WeightedRateCardRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted rate card rollup. Its code is unique within a tenant. */
data class WeightedRateCardRollup(
    var code: String,
    var name: String,
    var odometer: Int,
    var cost: Double,
    var channel: String,
    var dock: String,
    var maximum: Double,
    var status: WeightedRateCardRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRateCardRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRateCardRollupStatus): Boolean {
        when (status) {
            WeightedRateCardRollupStatus.PLANNED -> return next == WeightedRateCardRollupStatus.IN_PROGRESS || next == WeightedRateCardRollupStatus.CANCELLED
            WeightedRateCardRollupStatus.SCHEDULED -> {
                when (next) {
                    WeightedRateCardRollupStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedRateCardRollupStatus.IN_PROGRESS -> return next == WeightedRateCardRollupStatus.COMPLETED
            WeightedRateCardRollupStatus.COMPLETED -> return next == WeightedRateCardRollupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedRateCardRollupStatus) {
        if (!canMoveTo(next)) {
            error("weighted rate card rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedExposure(): Double = round((cost + maximum) * 100) / 100

    fun snapshot(): WeightedRateCardRollup {
        return WeightedRateCardRollup(
            code = code,
            name = name,
            odometer = odometer,
            cost = cost,
            channel = channel,
            dock = dock,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted rate card rollup in status planned. */
fun newWeightedRateCardRollup(code: String, name: String): WeightedRateCardRollup {
    return WeightedRateCardRollup(
        code = code,
        name = name,
        odometer = 0,
        cost = 0.0,
        channel = "",
        dock = "",
        maximum = 0.0,
        status = WeightedRateCardRollupStatus.PLANNED,
    )
}
