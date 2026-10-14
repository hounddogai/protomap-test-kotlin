package com.acme.transport.crewing.roster.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the roster window calculation. */
interface RosterWindowFactor {
    fun describe(): String
    fun adjust(item: RosterWindow, value: Double): Double
}

data class RosterWindowBudgetOffsetAdjuster(var weight: Double) : RosterWindowFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: RosterWindow, value: Double): Double = value + item.budget * weight
}

data class RoundingRosterWindowAdjuster(var step: Double) : RosterWindowFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterWindow, value: Double): Double = roundTo(value, step)
}

data class FlaggedRosterWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RosterWindowFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RosterWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRosterWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterWindowFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRosterWindowAdjuster(var minimum: Double) : RosterWindowFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RosterWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the roster window steps in order and keeps a trace of each. */
data class RosterWindowEngine(
    var steps: MutableList<RosterWindowFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RosterWindow): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterWindowFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RosterWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRosterWindowEngine(flags: Registry): RosterWindowEngine {
    return RosterWindowEngine(
        steps = mutableListOf<RosterWindowFactor>(
            RosterWindowBudgetOffsetAdjuster(1.32),
            RoundingRosterWindowAdjuster(0.1),
            FlaggedRosterWindowAdjuster(flags, "roster-window-primary", -3.1),
            TieredRosterWindowAdjuster(mutableListOf<Double>(377.0, 8937.0), mutableListOf<Double>(1.03, 1.19)),
            MinimumRosterWindowAdjuster(8.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 93188.0,
    )
}
