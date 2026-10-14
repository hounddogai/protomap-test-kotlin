package com.acme.transport.crewing.roster.milestone

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the roster assignment calculation. */
interface RosterAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: RosterAssignment, value: Double): Double
}

data class TieredRosterAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterAssignmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameRosterAssignmentAdjuster(var factors: MutableMap<String, Double>) : RosterAssignmentAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: RosterAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRosterAssignmentAdjuster(var percent: Double) : RosterAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRosterAssignmentAdjuster(var minimum: Double) : RosterAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RosterAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRosterAssignmentAdjuster(var cap: Double) : RosterAssignmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterAssignment, value: Double): Double = minOf(value, cap)
}

/** Runs the roster assignment steps in order and keeps a trace of each. */
data class RosterAssignmentEngine(
    var steps: MutableList<RosterAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RosterAssignment): Double {
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

    fun evaluateAll(items: MutableList<RosterAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRosterAssignmentEngine(): RosterAssignmentEngine {
    return RosterAssignmentEngine(
        steps = mutableListOf<RosterAssignmentAdjuster>(
            TieredRosterAssignmentAdjuster(
                mutableListOf<Double>(69.0, 6103.0),
                mutableListOf<Double>(0.85, 1.05),
            ),
            NameRosterAssignmentAdjuster(
                mutableMapOf<String, Double>("east" to 0.94, "west" to 1.09, "retail" to 1.37),
            ),
            PercentRosterAssignmentAdjuster(21.2),
            MinimumRosterAssignmentAdjuster(46.6),
            CapRosterAssignmentAdjuster(7072.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 86549.5,
    )
}
