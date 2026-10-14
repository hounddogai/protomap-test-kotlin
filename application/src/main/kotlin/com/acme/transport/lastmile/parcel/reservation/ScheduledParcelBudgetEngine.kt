package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled parcel budget calculation. */
interface ScheduledParcelBudgetStep {
    fun describe(): String
    fun adjust(item: ScheduledParcelBudget, value: Double): Double
}

data class PercentScheduledParcelBudgetAdjuster(var percent: Double) : ScheduledParcelBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledParcelBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapScheduledParcelBudgetAdjuster(var cap: Double) : ScheduledParcelBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledParcelBudget, value: Double): Double = minOf(value, cap)
}

data class RoundingScheduledParcelBudgetAdjuster(var step: Double) : ScheduledParcelBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledParcelBudget, value: Double): Double = roundTo(value, step)
}

data class ScheduledParcelBudgetVersionOffsetAdjuster(var weight: Double) : ScheduledParcelBudgetStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: ScheduledParcelBudget, value: Double): Double = value + item.version.toDouble() * weight
}

/** Runs the scheduled parcel budget steps in order and keeps a trace of each. */
data class ScheduledParcelBudgetEngine(
    var steps: MutableList<ScheduledParcelBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledParcelBudget): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledParcelBudgetStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledParcelBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledParcelBudgetEngine(): ScheduledParcelBudgetEngine {
    return ScheduledParcelBudgetEngine(
        steps = mutableListOf<ScheduledParcelBudgetStep>(
            PercentScheduledParcelBudgetAdjuster(3.2),
            CapScheduledParcelBudgetAdjuster(27565.0),
            RoundingScheduledParcelBudgetAdjuster(0.1),
            ScheduledParcelBudgetVersionOffsetAdjuster(2.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 1584.5,
    )
}
