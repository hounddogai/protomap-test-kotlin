package com.acme.transport.crewing.roster.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global roster group calculation. */
interface GlobalRosterGroupAdjuster {
    fun describe(): String
    fun adjust(item: GlobalRosterGroup, value: Double): Double
}

data class MinimumGlobalRosterGroupAdjuster(var minimum: Double) : GlobalRosterGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalRosterGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingGlobalRosterGroupAdjuster(var step: Double) : GlobalRosterGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalRosterGroup, value: Double): Double = roundTo(value, step)
}

data class GlobalRosterGroupQuantityOffsetAdjuster(var weight: Double) : GlobalRosterGroupAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: GlobalRosterGroup, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class FlaggedGlobalRosterGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalRosterGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalRosterGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentGlobalRosterGroupAdjuster(var percent: Double) : GlobalRosterGroupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalRosterGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredGlobalRosterGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalRosterGroupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalRosterGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global roster group steps in order and keeps a trace of each. */
data class GlobalRosterGroupEngine(
    var steps: MutableList<GlobalRosterGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalRosterGroup): Double {
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

    fun evaluateAll(items: MutableList<GlobalRosterGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalRosterGroupEngine(flags: Registry): GlobalRosterGroupEngine {
    return GlobalRosterGroupEngine(
        steps = mutableListOf<GlobalRosterGroupAdjuster>(
            MinimumGlobalRosterGroupAdjuster(32.6),
            RoundingGlobalRosterGroupAdjuster(1.0),
            GlobalRosterGroupQuantityOffsetAdjuster(0.2),
            FlaggedGlobalRosterGroupAdjuster(flags, "global-roster-group-batched", 4.9),
            PercentGlobalRosterGroupAdjuster(-0.3),
            TieredGlobalRosterGroupAdjuster(
                mutableListOf<Double>(1289.0, 1449.0, 2262.0, 5601.0),
                mutableListOf<Double>(1.03, 1.14, 1.11, 0.8),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 52137.6,
    )
}
