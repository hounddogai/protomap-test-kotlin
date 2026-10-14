package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the carbon report budget calculation. */
interface CarbonReportBudgetModifier {
    fun describe(): String
    fun adjust(item: CarbonReportBudget, value: Double): Double
}

data class TieredCarbonReportBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CarbonReportBudgetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CarbonReportBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCarbonReportBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportBudgetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCarbonReportBudgetAdjuster(var cap: Double) : CarbonReportBudgetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the carbon report budget steps in order and keeps a trace of each. */
data class CarbonReportBudgetEngine(
    var steps: MutableList<CarbonReportBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CarbonReportBudget): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CarbonReportBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCarbonReportBudgetEngine(flags: Registry): CarbonReportBudgetEngine {
    return CarbonReportBudgetEngine(
        steps = mutableListOf<CarbonReportBudgetModifier>(
            TieredCarbonReportBudgetAdjuster(
                mutableListOf<Double>(7818.0, 9679.0),
                mutableListOf<Double>(1.0, 0.95),
            ),
            FlaggedCarbonReportBudgetAdjuster(flags, "carbon-report-budget-scheduled", 10.0),
            CapCarbonReportBudgetAdjuster(24846.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 93412.1,
    )
}
