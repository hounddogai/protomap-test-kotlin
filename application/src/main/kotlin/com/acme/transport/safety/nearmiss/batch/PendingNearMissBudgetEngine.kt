package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending near miss budget calculation. */
interface PendingNearMissBudgetModifier {
    fun describe(): String
    fun adjust(item: PendingNearMissBudget, value: Double): Double
}

data class RoundingPendingNearMissBudgetAdjuster(var step: Double) : PendingNearMissBudgetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingNearMissBudget, value: Double): Double = roundTo(value, step)
}

data class TieredPendingNearMissBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingNearMissBudgetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingNearMissBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitlePendingNearMissBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingNearMissBudgetModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingNearMissBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPendingNearMissBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingNearMissBudgetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingNearMissBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPendingNearMissBudgetAdjuster(var cap: Double) : PendingNearMissBudgetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingNearMissBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the pending near miss budget steps in order and keeps a trace of each. */
data class PendingNearMissBudgetEngine(
    var steps: MutableList<PendingNearMissBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingNearMissBudget): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingNearMissBudgetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingNearMissBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingNearMissBudgetEngine(flags: Registry): PendingNearMissBudgetEngine {
    return PendingNearMissBudgetEngine(
        steps = mutableListOf<PendingNearMissBudgetModifier>(
            RoundingPendingNearMissBudgetAdjuster(5.0),
            TieredPendingNearMissBudgetAdjuster(
                mutableListOf<Double>(8510.0, 9075.0, 9581.0),
                mutableListOf<Double>(1.09, 0.91, 0.99),
            ),
            TitlePendingNearMissBudgetAdjuster(mutableMapOf<String, Double>("west" to 0.89, "retail" to 0.78)),
            FlaggedPendingNearMissBudgetAdjuster(flags, "pending-near-miss-budget-batched", -7.4),
            CapPendingNearMissBudgetAdjuster(47837.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 51067.2,
    )
}
