package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the parcel metric calculation. */
interface ParcelMetricModifier {
    fun describe(): String
    fun adjust(item: ParcelMetric, value: Double): Double
}

data class LabelParcelMetricAdjuster(var factors: MutableMap<String, Double>) : ParcelMetricModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: ParcelMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ParcelMetricCostOffsetAdjuster(var weight: Double) : ParcelMetricModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: ParcelMetric, value: Double): Double = value + item.cost * weight
}

data class PercentParcelMetricAdjuster(var percent: Double) : ParcelMetricModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumParcelMetricAdjuster(var minimum: Double) : ParcelMetricModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredParcelMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ParcelMetricModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ParcelMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingParcelMetricAdjuster(var step: Double) : ParcelMetricModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ParcelMetric, value: Double): Double = roundTo(value, step)
}

/** Runs the parcel metric steps in order and keeps a trace of each. */
data class ParcelMetricEngine(
    var steps: MutableList<ParcelMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ParcelMetric): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ParcelMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultParcelMetricEngine(): ParcelMetricEngine {
    return ParcelMetricEngine(
        steps = mutableListOf<ParcelMetricModifier>(
            LabelParcelMetricAdjuster(
                mutableMapOf<String, Double>("online" to 1.35, "retail" to 1.33, "central" to 1.1),
            ),
            ParcelMetricCostOffsetAdjuster(0.63),
            PercentParcelMetricAdjuster(7.3),
            MinimumParcelMetricAdjuster(25.8),
            TieredParcelMetricAdjuster(
                mutableListOf<Double>(746.0, 2220.0, 6982.0, 8569.0),
                mutableListOf<Double>(0.97, 1.09, 1.09, 0.97),
            ),
            RoundingParcelMetricAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 88328.5,
    )
}
