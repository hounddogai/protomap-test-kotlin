package com.acme.transport.crewing.roster.digest

import com.acme.transport.kit.numbers.roundTo

/** One step of the roster budget calculation. */
interface RosterBudgetFactor {
    fun describe(): String
    fun adjust(item: RosterBudget, value: Double): Double
}

data class RegionRosterBudgetAdjuster(var factors: MutableMap<String, Double>) : RosterBudgetFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: RosterBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRosterBudgetAdjuster(var cap: Double) : RosterBudgetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterBudget, value: Double): Double = minOf(value, cap)
}

data class TieredRosterBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterBudgetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRosterBudgetAdjuster(var step: Double) : RosterBudgetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterBudget, value: Double): Double = roundTo(value, step)
}

/** Runs the roster budget steps in order and keeps a trace of each. */
data class RosterBudgetEngine(
    var steps: MutableList<RosterBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RosterBudget): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterBudgetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RosterBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRosterBudgetEngine(): RosterBudgetEngine {
    return RosterBudgetEngine(
        steps = mutableListOf<RosterBudgetFactor>(
            RegionRosterBudgetAdjuster(mutableMapOf<String, Double>("east" to 0.93, "north" to 0.93)),
            CapRosterBudgetAdjuster(13419.0),
            TieredRosterBudgetAdjuster(
                mutableListOf<Double>(4327.0, 5968.0, 7195.0, 9667.0),
                mutableListOf<Double>(0.86, 1.03, 0.84, 1.0),
            ),
            RoundingRosterBudgetAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 5118.4,
    )
}
