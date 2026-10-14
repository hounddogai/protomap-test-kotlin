package com.acme.transport.emissions.emissionfactor.metric

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the emission factor metric calculation. */
interface EmissionFactorMetricFactor {
    fun describe(): String
    fun adjust(item: EmissionFactorMetric, value: Double): Double
}

data class RoundingEmissionFactorMetricAdjuster(var step: Double) : EmissionFactorMetricFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorMetric, value: Double): Double = roundTo(value, step)
}

data class EmissionFactorMetricEndsAtOffsetAdjuster(var weight: Double) : EmissionFactorMetricFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: EmissionFactorMetric, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class LabelEmissionFactorMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorMetricFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: EmissionFactorMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumEmissionFactorMetricAdjuster(var minimum: Double) : EmissionFactorMetricFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the emission factor metric steps in order and keeps a trace of each. */
data class EmissionFactorMetricEngine(
    var steps: MutableList<EmissionFactorMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EmissionFactorMetric): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EmissionFactorMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEmissionFactorMetricEngine(): EmissionFactorMetricEngine {
    return EmissionFactorMetricEngine(
        steps = mutableListOf<EmissionFactorMetricFactor>(
            RoundingEmissionFactorMetricAdjuster(0.25),
            EmissionFactorMetricEndsAtOffsetAdjuster(0.21),
            LabelEmissionFactorMetricAdjuster(mutableMapOf<String, Double>("retail" to 1.25)),
            MinimumEmissionFactorMetricAdjuster(22.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 12582.9,
    )
}
