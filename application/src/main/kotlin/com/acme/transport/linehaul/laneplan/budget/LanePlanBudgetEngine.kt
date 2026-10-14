package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.flags.Registry

/** One step of the lane plan budget calculation. */
interface LanePlanBudgetStep {
    fun describe(): String
    fun adjust(item: LanePlanBudget, value: Double): Double
}

data class FlaggedLanePlanBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LanePlanBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LanePlanBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLanePlanBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LanePlanBudgetEndsAtOffsetAdjuster(var weight: Double) : LanePlanBudgetStep {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: LanePlanBudget, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class CapLanePlanBudgetAdjuster(var cap: Double) : LanePlanBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the lane plan budget steps in order and keeps a trace of each. */
data class LanePlanBudgetEngine(
    var steps: MutableList<LanePlanBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LanePlanBudget): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LanePlanBudgetStep = steps[index]
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

    fun evaluateAll(items: MutableList<LanePlanBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLanePlanBudgetEngine(flags: Registry): LanePlanBudgetEngine {
    return LanePlanBudgetEngine(
        steps = mutableListOf<LanePlanBudgetStep>(
            FlaggedLanePlanBudgetAdjuster(flags, "lane-plan-budget-lenient", -2.3),
            TieredLanePlanBudgetAdjuster(
                mutableListOf<Double>(3833.0, 4227.0),
                mutableListOf<Double>(1.03, 1.03),
            ),
            LanePlanBudgetEndsAtOffsetAdjuster(0.76),
            CapLanePlanBudgetAdjuster(16830.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 60765.9,
    )
}
