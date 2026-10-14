package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional rate card snapshot calculation. */
interface RegionalRateCardSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: RegionalRateCardSnapshot, value: Double): Double
}

data class TieredRegionalRateCardSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalRateCardSnapshotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalRateCardSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRegionalRateCardSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalRateCardSnapshotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalRateCardSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionalRateCardSnapshotCeilingOffsetAdjuster(
    var weight: Double,
) : RegionalRateCardSnapshotAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: RegionalRateCardSnapshot, value: Double): Double = value + item.ceiling * weight
}

data class MinimumRegionalRateCardSnapshotAdjuster(var minimum: Double) : RegionalRateCardSnapshotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalRateCardSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRegionalRateCardSnapshotAdjuster(var step: Double) : RegionalRateCardSnapshotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalRateCardSnapshot, value: Double): Double = roundTo(value, step)
}

/** Runs the regional rate card snapshot steps in order and keeps a trace of each. */
data class RegionalRateCardSnapshotEngine(
    var steps: MutableList<RegionalRateCardSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalRateCardSnapshot): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalRateCardSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalRateCardSnapshotEngine(flags: Registry): RegionalRateCardSnapshotEngine {
    return RegionalRateCardSnapshotEngine(
        steps = mutableListOf<RegionalRateCardSnapshotAdjuster>(
            TieredRegionalRateCardSnapshotAdjuster(
                mutableListOf<Double>(825.0, 7409.0, 7564.0),
                mutableListOf<Double>(1.01, 1.07, 1.03),
            ),
            FlaggedRegionalRateCardSnapshotAdjuster(flags, "regional-rate-card-snapshot-legacy", -1.8),
            RegionalRateCardSnapshotCeilingOffsetAdjuster(0.67),
            MinimumRegionalRateCardSnapshotAdjuster(20.3),
            RoundingRegionalRateCardSnapshotAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 4390.4,
    )
}
