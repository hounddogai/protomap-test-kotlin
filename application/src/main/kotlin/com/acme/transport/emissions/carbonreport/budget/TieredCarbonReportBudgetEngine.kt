package com.acme.transport.emissions.carbonreport.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered carbon report budget calculation. */
interface TieredCarbonReportBudgetAdjuster {
    fun describe(): String
    fun adjust(item: TieredCarbonReportBudget, value: Double): Double
}

data class PercentTieredCarbonReportBudgetAdjuster(var percent: Double) : TieredCarbonReportBudgetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredCarbonReportBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumTieredCarbonReportBudgetAdjuster(var minimum: Double) : TieredCarbonReportBudgetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredCarbonReportBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTieredCarbonReportBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredCarbonReportBudgetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredCarbonReportBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTieredCarbonReportBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredCarbonReportBudgetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredCarbonReportBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTieredCarbonReportBudgetAdjuster(var step: Double) : TieredCarbonReportBudgetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredCarbonReportBudget, value: Double): Double = roundTo(value, step)
}

data class CapTieredCarbonReportBudgetAdjuster(var cap: Double) : TieredCarbonReportBudgetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredCarbonReportBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered carbon report budget steps in order and keeps a trace of each. */
data class TieredCarbonReportBudgetEngine(
    var steps: MutableList<TieredCarbonReportBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredCarbonReportBudget): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredCarbonReportBudgetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredCarbonReportBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredCarbonReportBudgetEngine(flags: Registry): TieredCarbonReportBudgetEngine {
    return TieredCarbonReportBudgetEngine(
        steps = mutableListOf<TieredCarbonReportBudgetAdjuster>(
            PercentTieredCarbonReportBudgetAdjuster(24.4),
            MinimumTieredCarbonReportBudgetAdjuster(11.4),
            FlaggedTieredCarbonReportBudgetAdjuster(flags, "tiered-carbon-report-budget-primary", 0.7),
            TieredTieredCarbonReportBudgetAdjuster(
                mutableListOf<Double>(1505.0, 2619.0, 5678.0, 6934.0),
                mutableListOf<Double>(0.84, 0.84, 0.99, 1.13),
            ),
            RoundingTieredCarbonReportBudgetAdjuster(0.5),
            CapTieredCarbonReportBudgetAdjuster(36073.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 26517.3,
    )
}
