package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the transit time budget calculation. */
interface TransitTimeBudgetAdjuster {
    fun describe(): String
    fun adjust(item: TransitTimeBudget, value: Double): Double
}

data class FlaggedTransitTimeBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TransitTimeBudgetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TransitTimeBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTransitTimeBudgetAdjuster(var step: Double) : TransitTimeBudgetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimeBudget, value: Double): Double = roundTo(value, step)
}

data class CapTransitTimeBudgetAdjuster(var cap: Double) : TransitTimeBudgetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimeBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the transit time budget steps in order and keeps a trace of each. */
data class TransitTimeBudgetEngine(
    var steps: MutableList<TransitTimeBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TransitTimeBudget): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TransitTimeBudgetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TransitTimeBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTransitTimeBudgetEngine(flags: Registry): TransitTimeBudgetEngine {
    return TransitTimeBudgetEngine(
        steps = mutableListOf<TransitTimeBudgetAdjuster>(
            FlaggedTransitTimeBudgetAdjuster(flags, "transit-time-budget-default", 8.3),
            RoundingTransitTimeBudgetAdjuster(0.25),
            CapTransitTimeBudgetAdjuster(39985.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 32256.6,
    )
}
