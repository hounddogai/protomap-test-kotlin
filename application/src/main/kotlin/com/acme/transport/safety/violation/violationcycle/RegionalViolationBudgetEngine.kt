package com.acme.transport.safety.violation.violationcycle

import com.acme.transport.kit.flags.Registry

/** One step of the regional violation budget calculation. */
interface RegionalViolationBudgetAdjuster {
    fun describe(): String
    fun adjust(item: RegionalViolationBudget, value: Double): Double
}

data class NameRegionalViolationBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalViolationBudgetAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalViolationBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRegionalViolationBudgetAdjuster(var cap: Double) : RegionalViolationBudgetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalViolationBudget, value: Double): Double = minOf(value, cap)
}

data class RegionalViolationBudgetRateOffsetAdjuster(var weight: Double) : RegionalViolationBudgetAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: RegionalViolationBudget, value: Double): Double = value + item.rate * weight
}

data class FlaggedRegionalViolationBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalViolationBudgetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalViolationBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional violation budget steps in order and keeps a trace of each. */
data class RegionalViolationBudgetEngine(
    var steps: MutableList<RegionalViolationBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalViolationBudget): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalViolationBudgetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalViolationBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalViolationBudgetEngine(flags: Registry): RegionalViolationBudgetEngine {
    return RegionalViolationBudgetEngine(
        steps = mutableListOf<RegionalViolationBudgetAdjuster>(
            NameRegionalViolationBudgetAdjuster(
                mutableMapOf<String, Double>("retail" to 1.32, "online" to 0.71, "west" to 0.97),
            ),
            CapRegionalViolationBudgetAdjuster(38140.0),
            RegionalViolationBudgetRateOffsetAdjuster(0.34),
            FlaggedRegionalViolationBudgetAdjuster(flags, "regional-violation-budget-legacy", -1.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 40986.9,
    )
}
