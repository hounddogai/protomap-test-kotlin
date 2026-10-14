package com.acme.transport.crewing.roster.rostercycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback roster cycle calculation. */
interface FallbackRosterCycleAdjuster {
    fun describe(): String
    fun adjust(item: FallbackRosterCycle, value: Double): Double
}

data class MinimumFallbackRosterCycleAdjuster(var minimum: Double) : FallbackRosterCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackRosterCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedFallbackRosterCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackRosterCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackRosterCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingFallbackRosterCycleAdjuster(var step: Double) : FallbackRosterCycleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackRosterCycle, value: Double): Double = roundTo(value, step)
}

data class TieredFallbackRosterCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackRosterCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackRosterCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback roster cycle steps in order and keeps a trace of each. */
data class FallbackRosterCycleEngine(
    var steps: MutableList<FallbackRosterCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackRosterCycle): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackRosterCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackRosterCycleEngine(flags: Registry): FallbackRosterCycleEngine {
    return FallbackRosterCycleEngine(
        steps = mutableListOf<FallbackRosterCycleAdjuster>(
            MinimumFallbackRosterCycleAdjuster(1.0),
            FlaggedFallbackRosterCycleAdjuster(flags, "fallback-roster-cycle-tiered", -0.4),
            RoundingFallbackRosterCycleAdjuster(0.5),
            TieredFallbackRosterCycleAdjuster(
                mutableListOf<Double>(561.0, 2973.0, 6222.0),
                mutableListOf<Double>(1.09, 0.95, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 66680.8,
    )
}
