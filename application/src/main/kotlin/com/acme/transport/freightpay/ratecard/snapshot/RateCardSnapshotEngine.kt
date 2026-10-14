package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rate card snapshot calculation. */
interface RateCardSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: RateCardSnapshot, value: Double): Double
}

data class MinimumRateCardSnapshotAdjuster(var minimum: Double) : RateCardSnapshotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRateCardSnapshotAdjuster(var percent: Double) : RateCardSnapshotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRateCardSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardSnapshotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rate card snapshot steps in order and keeps a trace of each. */
data class RateCardSnapshotEngine(
    var steps: MutableList<RateCardSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RateCardSnapshot): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RateCardSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRateCardSnapshotEngine(flags: Registry): RateCardSnapshotEngine {
    return RateCardSnapshotEngine(
        steps = mutableListOf<RateCardSnapshotAdjuster>(
            MinimumRateCardSnapshotAdjuster(26.5),
            PercentRateCardSnapshotAdjuster(0.6),
            FlaggedRateCardSnapshotAdjuster(flags, "rate-card-snapshot-weighted", 6.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 55808.6,
    )
}
