package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the carbon report milestone calculation. */
interface CarbonReportMilestoneStep {
    fun describe(): String
    fun adjust(item: CarbonReportMilestone, value: Double): Double
}

data class RoundingCarbonReportMilestoneAdjuster(var step: Double) : CarbonReportMilestoneStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CarbonReportMilestone, value: Double): Double = roundTo(value, step)
}

data class MinimumCarbonReportMilestoneAdjuster(var minimum: Double) : CarbonReportMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CarbonReportMilestoneGraceDaysOffsetAdjuster(var weight: Double) : CarbonReportMilestoneStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: CarbonReportMilestone, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class PercentCarbonReportMilestoneAdjuster(var percent: Double) : CarbonReportMilestoneStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCarbonReportMilestoneAdjuster(var cap: Double) : CarbonReportMilestoneStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportMilestone, value: Double): Double = minOf(value, cap)
}

data class DepotCarbonReportMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : CarbonReportMilestoneStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: CarbonReportMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the carbon report milestone steps in order and keeps a trace of each. */
data class CarbonReportMilestoneEngine(
    var steps: MutableList<CarbonReportMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CarbonReportMilestone): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CarbonReportMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCarbonReportMilestoneEngine(): CarbonReportMilestoneEngine {
    return CarbonReportMilestoneEngine(
        steps = mutableListOf<CarbonReportMilestoneStep>(
            RoundingCarbonReportMilestoneAdjuster(0.05),
            MinimumCarbonReportMilestoneAdjuster(24.9),
            CarbonReportMilestoneGraceDaysOffsetAdjuster(0.02),
            PercentCarbonReportMilestoneAdjuster(23.4),
            CapCarbonReportMilestoneAdjuster(36101.0),
            DepotCarbonReportMilestoneAdjuster(
                mutableMapOf<String, Double>("retail" to 0.79, "central" to 1.25),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 57921.5,
    )
}
