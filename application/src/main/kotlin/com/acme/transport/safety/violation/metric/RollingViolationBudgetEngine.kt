package com.acme.transport.safety.violation.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling violation budget calculation. */
interface RollingViolationBudgetModifier {
    fun describe(): String
    fun adjust(item: RollingViolationBudget, value: Double): Double
}

data class TitleRollingViolationBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingViolationBudgetModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: RollingViolationBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRollingViolationBudgetAdjuster(var percent: Double) : RollingViolationBudgetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingViolationBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRollingViolationBudgetAdjuster(var step: Double) : RollingViolationBudgetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingViolationBudget, value: Double): Double = roundTo(value, step)
}

data class FlaggedRollingViolationBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingViolationBudgetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingViolationBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling violation budget steps in order and keeps a trace of each. */
data class RollingViolationBudgetEngine(
    var steps: MutableList<RollingViolationBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingViolationBudget): Double {
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

    fun evaluateAll(items: MutableList<RollingViolationBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingViolationBudgetEngine(flags: Registry): RollingViolationBudgetEngine {
    return RollingViolationBudgetEngine(
        steps = mutableListOf<RollingViolationBudgetModifier>(
            TitleRollingViolationBudgetAdjuster(mutableMapOf<String, Double>("east" to 1.26)),
            PercentRollingViolationBudgetAdjuster(18.9),
            RoundingRollingViolationBudgetAdjuster(0.5),
            FlaggedRollingViolationBudgetAdjuster(flags, "rolling-violation-budget-pending", -8.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 6343.9,
    )
}
