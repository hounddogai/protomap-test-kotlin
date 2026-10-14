package com.acme.transport.crewing.roster.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the roster rollup calculation. */
interface RosterRollupModifier {
    fun describe(): String
    fun adjust(item: RosterRollup, value: Double): Double
}

data class DepotRosterRollupAdjuster(var factors: MutableMap<String, Double>) : RosterRollupModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: RosterRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRosterRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RosterRollupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RosterRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRosterRollupAdjuster(var minimum: Double) : RosterRollupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RosterRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRosterRollupAdjuster(var step: Double) : RosterRollupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterRollup, value: Double): Double = roundTo(value, step)
}

data class PercentRosterRollupAdjuster(var percent: Double) : RosterRollupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the roster rollup steps in order and keeps a trace of each. */
data class RosterRollupEngine(
    var steps: MutableList<RosterRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RosterRollup): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RosterRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRosterRollupEngine(flags: Registry): RosterRollupEngine {
    return RosterRollupEngine(
        steps = mutableListOf<RosterRollupModifier>(
            DepotRosterRollupAdjuster(
                mutableMapOf<String, Double>("east" to 1.26, "south" to 1.09, "central" to 0.71),
            ),
            FlaggedRosterRollupAdjuster(flags, "roster-rollup-pending", -7.2),
            MinimumRosterRollupAdjuster(24.0),
            RoundingRosterRollupAdjuster(0.1),
            PercentRosterRollupAdjuster(-20.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 71125.4,
    )
}
