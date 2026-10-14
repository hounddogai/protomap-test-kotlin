package com.acme.transport.linehaul.trailer.overrideitem

import com.acme.transport.kit.flags.Registry

/** One step of the trailer cycle calculation. */
interface TrailerCycleFactor {
    fun describe(): String
    fun adjust(item: TrailerCycle, value: Double): Double
}

data class MinimumTrailerCycleAdjuster(var minimum: Double) : TrailerCycleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTrailerCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerCycleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTrailerCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerCycleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTrailerCycleAdjuster(var cap: Double) : TrailerCycleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerCycle, value: Double): Double = minOf(value, cap)
}

/** Runs the trailer cycle steps in order and keeps a trace of each. */
data class TrailerCycleEngine(
    var steps: MutableList<TrailerCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerCycle): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerCycleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerCycleEngine(flags: Registry): TrailerCycleEngine {
    return TrailerCycleEngine(
        steps = mutableListOf<TrailerCycleFactor>(
            MinimumTrailerCycleAdjuster(47.1),
            FlaggedTrailerCycleAdjuster(flags, "trailer-cycle-capped", 3.3),
            TieredTrailerCycleAdjuster(mutableListOf<Double>(146.0, 5655.0), mutableListOf<Double>(0.9, 1.13)),
            CapTrailerCycleAdjuster(49745.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 62253.4,
    )
}
