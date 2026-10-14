package com.acme.transport.fleetops.tire.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional tire batch calculation. */
interface RegionalTireBatchFactor {
    fun describe(): String
    fun adjust(item: RegionalTireBatch, value: Double): Double
}

data class CapRegionalTireBatchAdjuster(var cap: Double) : RegionalTireBatchFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalTireBatch, value: Double): Double = minOf(value, cap)
}

data class MinimumRegionalTireBatchAdjuster(var minimum: Double) : RegionalTireBatchFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalTireBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionalTireBatchWeightOffsetAdjuster(var weight: Double) : RegionalTireBatchFactor {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: RegionalTireBatch, value: Double): Double = value + item.weight * weight
}

data class TieredRegionalTireBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalTireBatchFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalTireBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRegionalTireBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalTireBatchFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalTireBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional tire batch steps in order and keeps a trace of each. */
data class RegionalTireBatchEngine(
    var steps: MutableList<RegionalTireBatchFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalTireBatch): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalTireBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalTireBatchEngine(flags: Registry): RegionalTireBatchEngine {
    return RegionalTireBatchEngine(
        steps = mutableListOf<RegionalTireBatchFactor>(
            CapRegionalTireBatchAdjuster(3492.0),
            MinimumRegionalTireBatchAdjuster(26.9),
            RegionalTireBatchWeightOffsetAdjuster(1.9),
            TieredRegionalTireBatchAdjuster(
                mutableListOf<Double>(1383.0, 6507.0),
                mutableListOf<Double>(0.94, 0.91),
            ),
            FlaggedRegionalTireBatchAdjuster(flags, "regional-tire-batch-adaptive", 2.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 39600.8,
    )
}
