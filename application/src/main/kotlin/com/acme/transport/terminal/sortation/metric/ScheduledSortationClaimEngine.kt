package com.acme.transport.terminal.sortation.metric

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled sortation claim calculation. */
interface ScheduledSortationClaimStep {
    fun describe(): String
    fun adjust(item: ScheduledSortationClaim, value: Double): Double
}

data class PercentScheduledSortationClaimAdjuster(var percent: Double) : ScheduledSortationClaimStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledSortationClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ScheduledSortationClaimCreatedAtOffsetAdjuster(var weight: Double) : ScheduledSortationClaimStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: ScheduledSortationClaim, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class RoundingScheduledSortationClaimAdjuster(var step: Double) : ScheduledSortationClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledSortationClaim, value: Double): Double = roundTo(value, step)
}

data class LaneScheduledSortationClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledSortationClaimStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: ScheduledSortationClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled sortation claim steps in order and keeps a trace of each. */
data class ScheduledSortationClaimEngine(
    var steps: MutableList<ScheduledSortationClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledSortationClaim): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledSortationClaimStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledSortationClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledSortationClaimEngine(): ScheduledSortationClaimEngine {
    return ScheduledSortationClaimEngine(
        steps = mutableListOf<ScheduledSortationClaimStep>(
            PercentScheduledSortationClaimAdjuster(-7.9),
            ScheduledSortationClaimCreatedAtOffsetAdjuster(0.75),
            RoundingScheduledSortationClaimAdjuster(1.0),
            LaneScheduledSortationClaimAdjuster(
                mutableMapOf<String, Double>("central" to 1.04, "online" to 0.77),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 75636.1,
    )
}
