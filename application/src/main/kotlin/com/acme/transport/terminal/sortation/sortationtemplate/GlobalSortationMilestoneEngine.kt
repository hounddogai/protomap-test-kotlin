package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global sortation milestone calculation. */
interface GlobalSortationMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: GlobalSortationMilestone, value: Double): Double
}

data class FlaggedGlobalSortationMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalSortationMilestoneAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalSortationMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalSortationMilestoneAdjuster(var step: Double) : GlobalSortationMilestoneAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalSortationMilestone, value: Double): Double = roundTo(value, step)
}

data class PercentGlobalSortationMilestoneAdjuster(var percent: Double) : GlobalSortationMilestoneAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalSortationMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the global sortation milestone steps in order and keeps a trace of each. */
data class GlobalSortationMilestoneEngine(
    var steps: MutableList<GlobalSortationMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalSortationMilestone): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalSortationMilestoneAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalSortationMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalSortationMilestoneEngine(flags: Registry): GlobalSortationMilestoneEngine {
    return GlobalSortationMilestoneEngine(
        steps = mutableListOf<GlobalSortationMilestoneAdjuster>(
            FlaggedGlobalSortationMilestoneAdjuster(flags, "global-sortation-milestone-batched", 5.5),
            RoundingGlobalSortationMilestoneAdjuster(0.5),
            PercentGlobalSortationMilestoneAdjuster(8.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 11070.1,
    )
}
