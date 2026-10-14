package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled rest break budget calculation. */
interface ScheduledRestBreakBudgetModifier {
    fun describe(): String
    fun adjust(item: ScheduledRestBreakBudget, value: Double): Double
}

data class TitleScheduledRestBreakBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledRestBreakBudgetModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: ScheduledRestBreakBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingScheduledRestBreakBudgetAdjuster(var step: Double) : ScheduledRestBreakBudgetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledRestBreakBudget, value: Double): Double = roundTo(value, step)
}

data class TieredScheduledRestBreakBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledRestBreakBudgetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledRestBreakBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled rest break budget steps in order and keeps a trace of each. */
data class ScheduledRestBreakBudgetEngine(
    var steps: MutableList<ScheduledRestBreakBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledRestBreakBudget): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledRestBreakBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledRestBreakBudgetEngine(): ScheduledRestBreakBudgetEngine {
    return ScheduledRestBreakBudgetEngine(
        steps = mutableListOf<ScheduledRestBreakBudgetModifier>(
            TitleScheduledRestBreakBudgetAdjuster(mutableMapOf<String, Double>("south" to 0.81)),
            RoundingScheduledRestBreakBudgetAdjuster(0.25),
            TieredScheduledRestBreakBudgetAdjuster(
                mutableListOf<Double>(53.0, 1754.0, 2841.0, 5824.0),
                mutableListOf<Double>(1.18, 1.19, 0.81, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 5965.5,
    )
}
