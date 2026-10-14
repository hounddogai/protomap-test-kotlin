package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling roster rollup calculation. */
interface RollingRosterRollupModifier {
    fun describe(): String
    fun adjust(item: RollingRosterRollup, value: Double): Double
}

data class CapRollingRosterRollupAdjuster(var cap: Double) : RollingRosterRollupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingRosterRollup, value: Double): Double = minOf(value, cap)
}

data class RollingRosterRollupStopCountOffsetAdjuster(var weight: Double) : RollingRosterRollupModifier {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: RollingRosterRollup, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class RoundingRollingRosterRollupAdjuster(var step: Double) : RollingRosterRollupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingRosterRollup, value: Double): Double = roundTo(value, step)
}

data class MinimumRollingRosterRollupAdjuster(var minimum: Double) : RollingRosterRollupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingRosterRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CountryRollingRosterRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingRosterRollupModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: RollingRosterRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRollingRosterRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingRosterRollupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingRosterRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling roster rollup steps in order and keeps a trace of each. */
data class RollingRosterRollupEngine(
    var steps: MutableList<RollingRosterRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingRosterRollup): Double {
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

    fun evaluateAll(items: MutableList<RollingRosterRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingRosterRollupEngine(flags: Registry): RollingRosterRollupEngine {
    return RollingRosterRollupEngine(
        steps = mutableListOf<RollingRosterRollupModifier>(
            CapRollingRosterRollupAdjuster(22769.0),
            RollingRosterRollupStopCountOffsetAdjuster(0.29),
            RoundingRollingRosterRollupAdjuster(5.0),
            MinimumRollingRosterRollupAdjuster(37.7),
            CountryRollingRosterRollupAdjuster(mutableMapOf<String, Double>("online" to 1.31)),
            FlaggedRollingRosterRollupAdjuster(flags, "rolling-roster-rollup-fallback", -6.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 50653.1,
    )
}
