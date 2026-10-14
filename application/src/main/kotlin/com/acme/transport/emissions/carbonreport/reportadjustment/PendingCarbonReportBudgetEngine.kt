package com.acme.transport.emissions.carbonreport.reportadjustment

/** One step of the pending carbon report budget calculation. */
interface PendingCarbonReportBudgetStep {
    fun describe(): String
    fun adjust(item: PendingCarbonReportBudget, value: Double): Double
}

data class TieredPendingCarbonReportBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingCarbonReportBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingCarbonReportBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LanePendingCarbonReportBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingCarbonReportBudgetStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: PendingCarbonReportBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumPendingCarbonReportBudgetAdjuster(var minimum: Double) : PendingCarbonReportBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingCarbonReportBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending carbon report budget steps in order and keeps a trace of each. */
data class PendingCarbonReportBudgetEngine(
    var steps: MutableList<PendingCarbonReportBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingCarbonReportBudget): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingCarbonReportBudgetStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingCarbonReportBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingCarbonReportBudgetEngine(): PendingCarbonReportBudgetEngine {
    return PendingCarbonReportBudgetEngine(
        steps = mutableListOf<PendingCarbonReportBudgetStep>(
            TieredPendingCarbonReportBudgetAdjuster(
                mutableListOf<Double>(2381.0, 8135.0),
                mutableListOf<Double>(1.12, 0.86),
            ),
            LanePendingCarbonReportBudgetAdjuster(
                mutableMapOf<String, Double>("central" to 1.26, "retail" to 1.31, "south" to 1.37),
            ),
            MinimumPendingCarbonReportBudgetAdjuster(25.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 77221.4,
    )
}
