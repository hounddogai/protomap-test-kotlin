package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional carbon report budget calculation. */
interface RegionalCarbonReportBudgetStep {
    fun describe(): String
    fun adjust(item: RegionalCarbonReportBudget, value: Double): Double
}

data class CapRegionalCarbonReportBudgetAdjuster(var cap: Double) : RegionalCarbonReportBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalCarbonReportBudget, value: Double): Double = minOf(value, cap)
}

data class RegionalCarbonReportBudgetUpdatedAtOffsetAdjuster(
    var weight: Double,
) : RegionalCarbonReportBudgetStep {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: RegionalCarbonReportBudget, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class MinimumRegionalCarbonReportBudgetAdjuster(var minimum: Double) : RegionalCarbonReportBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalCarbonReportBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocaleRegionalCarbonReportBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCarbonReportBudgetStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: RegionalCarbonReportBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional carbon report budget steps in order and keeps a trace of each. */
data class RegionalCarbonReportBudgetEngine(
    var steps: MutableList<RegionalCarbonReportBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalCarbonReportBudget): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalCarbonReportBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalCarbonReportBudgetEngine(): RegionalCarbonReportBudgetEngine {
    return RegionalCarbonReportBudgetEngine(
        steps = mutableListOf<RegionalCarbonReportBudgetStep>(
            CapRegionalCarbonReportBudgetAdjuster(37021.0),
            RegionalCarbonReportBudgetUpdatedAtOffsetAdjuster(1.34),
            MinimumRegionalCarbonReportBudgetAdjuster(4.3),
            LocaleRegionalCarbonReportBudgetAdjuster(
                mutableMapOf<String, Double>("central" to 0.75, "online" to 1.05, "north" to 0.88),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 63097.9,
    )
}
