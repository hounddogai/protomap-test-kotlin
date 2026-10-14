package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the roster group calculation. */
interface RosterGroupModifier {
    fun describe(): String
    fun adjust(item: RosterGroup, value: Double): Double
}

data class RoundingRosterGroupAdjuster(var step: Double) : RosterGroupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterGroup, value: Double): Double = roundTo(value, step)
}

data class MinimumRosterGroupAdjuster(var minimum: Double) : RosterGroupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RosterGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRosterGroupAdjuster(var cap: Double) : RosterGroupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterGroup, value: Double): Double = minOf(value, cap)
}

data class TieredRosterGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterGroupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRosterGroupAdjuster(var percent: Double) : RosterGroupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRosterGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RosterGroupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RosterGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the roster group steps in order and keeps a trace of each. */
data class RosterGroupEngine(
    var steps: MutableList<RosterGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RosterGroup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterGroupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RosterGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRosterGroupEngine(flags: Registry): RosterGroupEngine {
    return RosterGroupEngine(
        steps = mutableListOf<RosterGroupModifier>(
            RoundingRosterGroupAdjuster(5.0),
            MinimumRosterGroupAdjuster(21.9),
            CapRosterGroupAdjuster(43692.0),
            TieredRosterGroupAdjuster(
                mutableListOf<Double>(426.0, 4291.0, 6384.0, 6695.0),
                mutableListOf<Double>(1.09, 0.84, 0.9, 1.06),
            ),
            PercentRosterGroupAdjuster(9.6),
            FlaggedRosterGroupAdjuster(flags, "roster-group-lenient", -6.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 38268.2,
    )
}
