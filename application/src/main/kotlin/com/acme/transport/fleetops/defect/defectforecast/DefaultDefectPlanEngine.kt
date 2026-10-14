package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.kit.numbers.roundTo

/** One step of the default defect plan calculation. */
interface DefaultDefectPlanAdjuster {
    fun describe(): String
    fun adjust(item: DefaultDefectPlan, value: Double): Double
}

data class DefaultDefectPlanEtaMinutesOffsetAdjuster(var weight: Double) : DefaultDefectPlanAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: DefaultDefectPlan, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class PercentDefaultDefectPlanAdjuster(var percent: Double) : DefaultDefectPlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultDefectPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDefaultDefectPlanAdjuster(var step: Double) : DefaultDefectPlanAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultDefectPlan, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultDefectPlanAdjuster(var minimum: Double) : DefaultDefectPlanAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultDefectPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default defect plan steps in order and keeps a trace of each. */
data class DefaultDefectPlanEngine(
    var steps: MutableList<DefaultDefectPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultDefectPlan): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultDefectPlanAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultDefectPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultDefectPlanEngine(): DefaultDefectPlanEngine {
    return DefaultDefectPlanEngine(
        steps = mutableListOf<DefaultDefectPlanAdjuster>(
            DefaultDefectPlanEtaMinutesOffsetAdjuster(1.5),
            PercentDefaultDefectPlanAdjuster(-19.0),
            RoundingDefaultDefectPlanAdjuster(0.05),
            MinimumDefaultDefectPlanAdjuster(16.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 34532.0,
    )
}
