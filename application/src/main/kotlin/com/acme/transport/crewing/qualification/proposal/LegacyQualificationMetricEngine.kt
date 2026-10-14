package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy qualification metric calculation. */
interface LegacyQualificationMetricModifier {
    fun describe(): String
    fun adjust(item: LegacyQualificationMetric, value: Double): Double
}

data class LabelLegacyQualificationMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyQualificationMetricModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: LegacyQualificationMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LegacyQualificationMetricAmountOffsetAdjuster(
    var weight: Double,
) : LegacyQualificationMetricModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: LegacyQualificationMetric, value: Double): Double = value + item.amount * weight
}

data class MinimumLegacyQualificationMetricAdjuster(var minimum: Double) : LegacyQualificationMetricModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyQualificationMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLegacyQualificationMetricAdjuster(var step: Double) : LegacyQualificationMetricModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyQualificationMetric, value: Double): Double = roundTo(value, step)
}

data class CapLegacyQualificationMetricAdjuster(var cap: Double) : LegacyQualificationMetricModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyQualificationMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy qualification metric steps in order and keeps a trace of each. */
data class LegacyQualificationMetricEngine(
    var steps: MutableList<LegacyQualificationMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyQualificationMetric): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyQualificationMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyQualificationMetricEngine(): LegacyQualificationMetricEngine {
    return LegacyQualificationMetricEngine(
        steps = mutableListOf<LegacyQualificationMetricModifier>(
            LabelLegacyQualificationMetricAdjuster(
                mutableMapOf<String, Double>("retail" to 1.02, "north" to 1.36, "south" to 1.21),
            ),
            LegacyQualificationMetricAmountOffsetAdjuster(0.65),
            MinimumLegacyQualificationMetricAdjuster(9.8),
            RoundingLegacyQualificationMetricAdjuster(5.0),
            CapLegacyQualificationMetricAdjuster(41381.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 2899.0,
    )
}
