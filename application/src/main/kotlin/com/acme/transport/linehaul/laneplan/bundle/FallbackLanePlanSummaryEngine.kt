package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback lane plan summary calculation. */
interface FallbackLanePlanSummaryStep {
    fun describe(): String
    fun adjust(item: FallbackLanePlanSummary, value: Double): Double
}

data class RoundingFallbackLanePlanSummaryAdjuster(var step: Double) : FallbackLanePlanSummaryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackLanePlanSummary, value: Double): Double = roundTo(value, step)
}

data class FallbackLanePlanSummaryOdometerOffsetAdjuster(var weight: Double) : FallbackLanePlanSummaryStep {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: FallbackLanePlanSummary, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class PercentFallbackLanePlanSummaryAdjuster(var percent: Double) : FallbackLanePlanSummaryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackLanePlanSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFallbackLanePlanSummaryAdjuster(var minimum: Double) : FallbackLanePlanSummaryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackLanePlanSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback lane plan summary steps in order and keeps a trace of each. */
data class FallbackLanePlanSummaryEngine(
    var steps: MutableList<FallbackLanePlanSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackLanePlanSummary): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackLanePlanSummaryStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackLanePlanSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackLanePlanSummaryEngine(): FallbackLanePlanSummaryEngine {
    return FallbackLanePlanSummaryEngine(
        steps = mutableListOf<FallbackLanePlanSummaryStep>(
            RoundingFallbackLanePlanSummaryAdjuster(0.1),
            FallbackLanePlanSummaryOdometerOffsetAdjuster(0.44),
            PercentFallbackLanePlanSummaryAdjuster(-19.2),
            MinimumFallbackLanePlanSummaryAdjuster(41.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 84584.1,
    )
}
