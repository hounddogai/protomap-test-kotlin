package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.flags.Registry

/** One step of the tire rollup calculation. */
interface TireRollupAdjuster {
    fun describe(): String
    fun adjust(item: TireRollup, value: Double): Double
}

data class FlaggedTireRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireRollupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTireRollupAdjuster(var minimum: Double) : TireRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TireRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTireRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tire rollup steps in order and keeps a trace of each. */
data class TireRollupEngine(
    var steps: MutableList<TireRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TireRollup): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TireRollupAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TireRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTireRollupEngine(flags: Registry): TireRollupEngine {
    return TireRollupEngine(
        steps = mutableListOf<TireRollupAdjuster>(
            FlaggedTireRollupAdjuster(flags, "tire-rollup-tiered", -1.5),
            MinimumTireRollupAdjuster(9.9),
            TieredTireRollupAdjuster(
                mutableListOf<Double>(704.0, 2069.0, 5255.0, 7166.0),
                mutableListOf<Double>(1.1, 1.19, 1.18, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 1862.5,
    )
}
