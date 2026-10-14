package com.acme.transport.customs.clearance.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global clearance milestone calculation. */
interface GlobalClearanceMilestoneStep {
    fun describe(): String
    fun adjust(item: GlobalClearanceMilestone, value: Double): Double
}

data class GlobalClearanceMilestoneSequenceOffsetAdjuster(var weight: Double) : GlobalClearanceMilestoneStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: GlobalClearanceMilestone, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class TitleGlobalClearanceMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalClearanceMilestoneStep {
    override fun describe(): String = "by title"

    override fun adjust(item: GlobalClearanceMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentGlobalClearanceMilestoneAdjuster(var percent: Double) : GlobalClearanceMilestoneStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalClearanceMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumGlobalClearanceMilestoneAdjuster(var minimum: Double) : GlobalClearanceMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalClearanceMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingGlobalClearanceMilestoneAdjuster(var step: Double) : GlobalClearanceMilestoneStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalClearanceMilestone, value: Double): Double = roundTo(value, step)
}

data class FlaggedGlobalClearanceMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalClearanceMilestoneStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalClearanceMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global clearance milestone steps in order and keeps a trace of each. */
data class GlobalClearanceMilestoneEngine(
    var steps: MutableList<GlobalClearanceMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalClearanceMilestone): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalClearanceMilestoneStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalClearanceMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalClearanceMilestoneEngine(flags: Registry): GlobalClearanceMilestoneEngine {
    return GlobalClearanceMilestoneEngine(
        steps = mutableListOf<GlobalClearanceMilestoneStep>(
            GlobalClearanceMilestoneSequenceOffsetAdjuster(1.84),
            TitleGlobalClearanceMilestoneAdjuster(
                mutableMapOf<String, Double>("online" to 0.73, "west" to 0.91, "east" to 1.22),
            ),
            PercentGlobalClearanceMilestoneAdjuster(-24.8),
            MinimumGlobalClearanceMilestoneAdjuster(20.2),
            RoundingGlobalClearanceMilestoneAdjuster(1.0),
            FlaggedGlobalClearanceMilestoneAdjuster(flags, "global-clearance-milestone-capped", 1.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 13280.8,
    )
}
