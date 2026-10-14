package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.flags.Registry

/** One step of the legacy scorecard budget calculation. */
interface LegacyScorecardBudgetModifier {
    fun describe(): String
    fun adjust(item: LegacyScorecardBudget, value: Double): Double
}

data class FlaggedLegacyScorecardBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyScorecardBudgetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyScorecardBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLegacyScorecardBudgetAdjuster(var cap: Double) : LegacyScorecardBudgetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyScorecardBudget, value: Double): Double = minOf(value, cap)
}

data class PercentLegacyScorecardBudgetAdjuster(var percent: Double) : LegacyScorecardBudgetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyScorecardBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LegacyScorecardBudgetVersionOffsetAdjuster(var weight: Double) : LegacyScorecardBudgetModifier {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: LegacyScorecardBudget, value: Double): Double = value + item.version.toDouble() * weight
}

data class MinimumLegacyScorecardBudgetAdjuster(var minimum: Double) : LegacyScorecardBudgetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyScorecardBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy scorecard budget steps in order and keeps a trace of each. */
data class LegacyScorecardBudgetEngine(
    var steps: MutableList<LegacyScorecardBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyScorecardBudget): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyScorecardBudgetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyScorecardBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyScorecardBudgetEngine(flags: Registry): LegacyScorecardBudgetEngine {
    return LegacyScorecardBudgetEngine(
        steps = mutableListOf<LegacyScorecardBudgetModifier>(
            FlaggedLegacyScorecardBudgetAdjuster(flags, "legacy-scorecard-budget-capped", 7.3),
            CapLegacyScorecardBudgetAdjuster(18939.0),
            PercentLegacyScorecardBudgetAdjuster(-12.5),
            LegacyScorecardBudgetVersionOffsetAdjuster(0.08),
            MinimumLegacyScorecardBudgetAdjuster(19.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 35198.8,
    )
}
