package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling roster entry calculation. */
interface RollingRosterEntryStep {
    fun describe(): String
    fun adjust(item: RollingRosterEntry, value: Double): Double
}

data class PercentRollingRosterEntryAdjuster(var percent: Double) : RollingRosterEntryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingRosterEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRollingRosterEntryAdjuster(var cap: Double) : RollingRosterEntryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingRosterEntry, value: Double): Double = minOf(value, cap)
}

data class RoundingRollingRosterEntryAdjuster(var step: Double) : RollingRosterEntryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingRosterEntry, value: Double): Double = roundTo(value, step)
}

data class TieredRollingRosterEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingRosterEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingRosterEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRollingRosterEntryAdjuster(var minimum: Double) : RollingRosterEntryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingRosterEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelRollingRosterEntryAdjuster(var factors: MutableMap<String, Double>) : RollingRosterEntryStep {
    override fun describe(): String = "by label"

    override fun adjust(item: RollingRosterEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rolling roster entry steps in order and keeps a trace of each. */
data class RollingRosterEntryEngine(
    var steps: MutableList<RollingRosterEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingRosterEntry): Double {
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

    fun evaluateAll(items: MutableList<RollingRosterEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingRosterEntryEngine(): RollingRosterEntryEngine {
    return RollingRosterEntryEngine(
        steps = mutableListOf<RollingRosterEntryStep>(
            PercentRollingRosterEntryAdjuster(-21.3),
            CapRollingRosterEntryAdjuster(42385.0),
            RoundingRollingRosterEntryAdjuster(1.0),
            TieredRollingRosterEntryAdjuster(
                mutableListOf<Double>(4539.0, 6278.0, 6771.0),
                mutableListOf<Double>(1.17, 1.19, 1.15),
            ),
            MinimumRollingRosterEntryAdjuster(17.9),
            LabelRollingRosterEntryAdjuster(
                mutableMapOf<String, Double>("online" to 0.71, "central" to 0.82, "west" to 1.36),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 22419.7,
    )
}
