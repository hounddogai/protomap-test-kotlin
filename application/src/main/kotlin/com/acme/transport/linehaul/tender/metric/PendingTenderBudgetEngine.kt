package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.flags.Registry

/** One step of the pending tender budget calculation. */
interface PendingTenderBudgetFactor {
    fun describe(): String
    fun adjust(item: PendingTenderBudget, value: Double): Double
}

data class FlaggedPendingTenderBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingTenderBudgetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingTenderBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingTenderBudgetAdjuster(var minimum: Double) : PendingTenderBudgetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingTenderBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPendingTenderBudgetAdjuster(var cap: Double) : PendingTenderBudgetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingTenderBudget, value: Double): Double = minOf(value, cap)
}

data class TieredPendingTenderBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingTenderBudgetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingTenderBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending tender budget steps in order and keeps a trace of each. */
data class PendingTenderBudgetEngine(
    var steps: MutableList<PendingTenderBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingTenderBudget): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingTenderBudgetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingTenderBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingTenderBudgetEngine(flags: Registry): PendingTenderBudgetEngine {
    return PendingTenderBudgetEngine(
        steps = mutableListOf<PendingTenderBudgetFactor>(
            FlaggedPendingTenderBudgetAdjuster(flags, "pending-tender-budget-batched", -2.6),
            MinimumPendingTenderBudgetAdjuster(48.9),
            CapPendingTenderBudgetAdjuster(4023.0),
            TieredPendingTenderBudgetAdjuster(
                mutableListOf<Double>(5062.0, 5836.0),
                mutableListOf<Double>(0.92, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 51232.8,
    )
}
