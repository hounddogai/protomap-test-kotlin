package com.acme.transport.emissions.routeoffset.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary route offset budget calculation. */
interface PrimaryRouteOffsetBudgetAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryRouteOffsetBudget, value: Double): Double
}

data class RoundingPrimaryRouteOffsetBudgetAdjuster(var step: Double) : PrimaryRouteOffsetBudgetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryRouteOffsetBudget, value: Double): Double = roundTo(value, step)
}

data class CapPrimaryRouteOffsetBudgetAdjuster(var cap: Double) : PrimaryRouteOffsetBudgetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryRouteOffsetBudget, value: Double): Double = minOf(value, cap)
}

data class CountryPrimaryRouteOffsetBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryRouteOffsetBudgetAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: PrimaryRouteOffsetBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPrimaryRouteOffsetBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryRouteOffsetBudgetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryRouteOffsetBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PrimaryRouteOffsetBudgetGraceDaysOffsetAdjuster(
    var weight: Double,
) : PrimaryRouteOffsetBudgetAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: PrimaryRouteOffsetBudget, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class TieredPrimaryRouteOffsetBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryRouteOffsetBudgetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryRouteOffsetBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary route offset budget steps in order and keeps a trace of each. */
data class PrimaryRouteOffsetBudgetEngine(
    var steps: MutableList<PrimaryRouteOffsetBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryRouteOffsetBudget): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryRouteOffsetBudgetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryRouteOffsetBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryRouteOffsetBudgetEngine(flags: Registry): PrimaryRouteOffsetBudgetEngine {
    return PrimaryRouteOffsetBudgetEngine(
        steps = mutableListOf<PrimaryRouteOffsetBudgetAdjuster>(
            RoundingPrimaryRouteOffsetBudgetAdjuster(5.0),
            CapPrimaryRouteOffsetBudgetAdjuster(19763.0),
            CountryPrimaryRouteOffsetBudgetAdjuster(
                mutableMapOf<String, Double>("west" to 0.73, "east" to 0.88),
            ),
            FlaggedPrimaryRouteOffsetBudgetAdjuster(flags, "primary-route-offset-budget-tiered", -6.8),
            PrimaryRouteOffsetBudgetGraceDaysOffsetAdjuster(0.11),
            TieredPrimaryRouteOffsetBudgetAdjuster(
                mutableListOf<Double>(3760.0, 6688.0),
                mutableListOf<Double>(0.92, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 29250.0,
    )
}
