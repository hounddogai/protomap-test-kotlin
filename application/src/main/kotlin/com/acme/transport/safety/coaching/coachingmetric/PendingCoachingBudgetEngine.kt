package com.acme.transport.safety.coaching.coachingmetric

import com.acme.transport.kit.flags.Registry

/** One step of the pending coaching budget calculation. */
interface PendingCoachingBudgetFactor {
    fun describe(): String
    fun adjust(item: PendingCoachingBudget, value: Double): Double
}

data class FlaggedPendingCoachingBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingCoachingBudgetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingCoachingBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingCoachingBudgetAdjuster(var minimum: Double) : PendingCoachingBudgetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingCoachingBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPendingCoachingBudgetAdjuster(var percent: Double) : PendingCoachingBudgetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingCoachingBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SegmentPendingCoachingBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingCoachingBudgetFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: PendingCoachingBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending coaching budget steps in order and keeps a trace of each. */
data class PendingCoachingBudgetEngine(
    var steps: MutableList<PendingCoachingBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingCoachingBudget): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingCoachingBudgetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingCoachingBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingCoachingBudgetEngine(flags: Registry): PendingCoachingBudgetEngine {
    return PendingCoachingBudgetEngine(
        steps = mutableListOf<PendingCoachingBudgetFactor>(
            FlaggedPendingCoachingBudgetAdjuster(flags, "pending-coaching-budget-scheduled", 9.8),
            MinimumPendingCoachingBudgetAdjuster(32.0),
            PercentPendingCoachingBudgetAdjuster(5.3),
            SegmentPendingCoachingBudgetAdjuster(
                mutableMapOf<String, Double>("south" to 1.03, "central" to 0.73, "retail" to 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 6248.8,
    )
}
