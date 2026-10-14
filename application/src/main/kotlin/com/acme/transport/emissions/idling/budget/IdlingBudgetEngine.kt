package com.acme.transport.emissions.idling.budget

import com.acme.transport.kit.flags.Registry

/** One step of the idling budget calculation. */
interface IdlingBudgetModifier {
    fun describe(): String
    fun adjust(item: IdlingBudget, value: Double): Double
}

data class IdlingBudgetScoreOffsetAdjuster(var weight: Double) : IdlingBudgetModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: IdlingBudget, value: Double): Double = value + item.score * weight
}

data class FlaggedIdlingBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingBudgetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapIdlingBudgetAdjuster(var cap: Double) : IdlingBudgetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingBudget, value: Double): Double = minOf(value, cap)
}

data class PercentIdlingBudgetAdjuster(var percent: Double) : IdlingBudgetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneIdlingBudgetAdjuster(var factors: MutableMap<String, Double>) : IdlingBudgetModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: IdlingBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumIdlingBudgetAdjuster(var minimum: Double) : IdlingBudgetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the idling budget steps in order and keeps a trace of each. */
data class IdlingBudgetEngine(
    var steps: MutableList<IdlingBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IdlingBudget): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingBudgetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIdlingBudgetEngine(flags: Registry): IdlingBudgetEngine {
    return IdlingBudgetEngine(
        steps = mutableListOf<IdlingBudgetModifier>(
            IdlingBudgetScoreOffsetAdjuster(0.97),
            FlaggedIdlingBudgetAdjuster(flags, "idling-budget-pending", -5.0),
            CapIdlingBudgetAdjuster(41172.0),
            PercentIdlingBudgetAdjuster(-5.8),
            LaneIdlingBudgetAdjuster(mutableMapOf<String, Double>("west" to 0.86)),
            MinimumIdlingBudgetAdjuster(48.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 7444.0,
    )
}
