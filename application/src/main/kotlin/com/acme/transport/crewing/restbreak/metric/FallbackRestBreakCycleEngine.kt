package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback rest break cycle calculation. */
interface FallbackRestBreakCycleAdjuster {
    fun describe(): String
    fun adjust(item: FallbackRestBreakCycle, value: Double): Double
}

data class CapFallbackRestBreakCycleAdjuster(var cap: Double) : FallbackRestBreakCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackRestBreakCycle, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackRestBreakCycleAdjuster(var percent: Double) : FallbackRestBreakCycleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackRestBreakCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredFallbackRestBreakCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackRestBreakCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackRestBreakCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedFallbackRestBreakCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackRestBreakCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackRestBreakCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFallbackRestBreakCycleAdjuster(var minimum: Double) : FallbackRestBreakCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackRestBreakCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFallbackRestBreakCycleAdjuster(var step: Double) : FallbackRestBreakCycleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackRestBreakCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback rest break cycle steps in order and keeps a trace of each. */
data class FallbackRestBreakCycleEngine(
    var steps: MutableList<FallbackRestBreakCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackRestBreakCycle): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackRestBreakCycleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackRestBreakCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackRestBreakCycleEngine(flags: Registry): FallbackRestBreakCycleEngine {
    return FallbackRestBreakCycleEngine(
        steps = mutableListOf<FallbackRestBreakCycleAdjuster>(
            CapFallbackRestBreakCycleAdjuster(3667.0),
            PercentFallbackRestBreakCycleAdjuster(-8.3),
            TieredFallbackRestBreakCycleAdjuster(
                mutableListOf<Double>(709.0, 3517.0, 9711.0),
                mutableListOf<Double>(0.96, 1.01, 1.06),
            ),
            FlaggedFallbackRestBreakCycleAdjuster(flags, "fallback-rest-break-cycle-legacy", 7.0),
            MinimumFallbackRestBreakCycleAdjuster(5.3),
            RoundingFallbackRestBreakCycleAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 5192.9,
    )
}
