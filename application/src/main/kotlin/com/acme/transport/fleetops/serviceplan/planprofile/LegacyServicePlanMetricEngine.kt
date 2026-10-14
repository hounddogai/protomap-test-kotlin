package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy service plan metric calculation. */
interface LegacyServicePlanMetricStep {
    fun describe(): String
    fun adjust(item: LegacyServicePlanMetric, value: Double): Double
}

data class MinimumLegacyServicePlanMetricAdjuster(var minimum: Double) : LegacyServicePlanMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyServicePlanMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLegacyServicePlanMetricAdjuster(var step: Double) : LegacyServicePlanMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyServicePlanMetric, value: Double): Double = roundTo(value, step)
}

data class CapLegacyServicePlanMetricAdjuster(var cap: Double) : LegacyServicePlanMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyServicePlanMetric, value: Double): Double = minOf(value, cap)
}

data class LegacyServicePlanMetricUpdatedAtOffsetAdjuster(var weight: Double) : LegacyServicePlanMetricStep {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: LegacyServicePlanMetric, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

/** Runs the legacy service plan metric steps in order and keeps a trace of each. */
data class LegacyServicePlanMetricEngine(
    var steps: MutableList<LegacyServicePlanMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyServicePlanMetric): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyServicePlanMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyServicePlanMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyServicePlanMetricEngine(): LegacyServicePlanMetricEngine {
    return LegacyServicePlanMetricEngine(
        steps = mutableListOf<LegacyServicePlanMetricStep>(
            MinimumLegacyServicePlanMetricAdjuster(33.2),
            RoundingLegacyServicePlanMetricAdjuster(0.5),
            CapLegacyServicePlanMetricAdjuster(42653.0),
            LegacyServicePlanMetricUpdatedAtOffsetAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 24519.8,
    )
}
