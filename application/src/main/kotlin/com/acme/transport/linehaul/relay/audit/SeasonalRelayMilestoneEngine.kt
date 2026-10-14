package com.acme.transport.linehaul.relay.audit

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal relay milestone calculation. */
interface SeasonalRelayMilestoneStep {
    fun describe(): String
    fun adjust(item: SeasonalRelayMilestone, value: Double): Double
}

data class SeasonalRelayMilestoneAttemptsOffsetAdjuster(var weight: Double) : SeasonalRelayMilestoneStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: SeasonalRelayMilestone, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class PercentSeasonalRelayMilestoneAdjuster(var percent: Double) : SeasonalRelayMilestoneStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalRelayMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SourceSeasonalRelayMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalRelayMilestoneStep {
    override fun describe(): String = "by source"

    override fun adjust(item: SeasonalRelayMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSeasonalRelayMilestoneAdjuster(var step: Double) : SeasonalRelayMilestoneStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalRelayMilestone, value: Double): Double = roundTo(value, step)
}

data class CapSeasonalRelayMilestoneAdjuster(var cap: Double) : SeasonalRelayMilestoneStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalRelayMilestone, value: Double): Double = minOf(value, cap)
}

/** Runs the seasonal relay milestone steps in order and keeps a trace of each. */
data class SeasonalRelayMilestoneEngine(
    var steps: MutableList<SeasonalRelayMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalRelayMilestone): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalRelayMilestoneStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalRelayMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalRelayMilestoneEngine(): SeasonalRelayMilestoneEngine {
    return SeasonalRelayMilestoneEngine(
        steps = mutableListOf<SeasonalRelayMilestoneStep>(
            SeasonalRelayMilestoneAttemptsOffsetAdjuster(0.87),
            PercentSeasonalRelayMilestoneAdjuster(23.8),
            SourceSeasonalRelayMilestoneAdjuster(mutableMapOf<String, Double>("north" to 1.26, "south" to 1.33)),
            RoundingSeasonalRelayMilestoneAdjuster(0.1),
            CapSeasonalRelayMilestoneAdjuster(37443.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 81111.6,
    )
}
