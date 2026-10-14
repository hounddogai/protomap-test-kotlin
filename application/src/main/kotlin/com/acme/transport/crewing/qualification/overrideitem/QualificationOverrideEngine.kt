package com.acme.transport.crewing.qualification.overrideitem

import com.acme.transport.kit.numbers.roundTo

/** One step of the qualification override calculation. */
interface QualificationOverrideFactor {
    fun describe(): String
    fun adjust(item: QualificationOverride, value: Double): Double
}

data class QualificationOverrideCostOffsetAdjuster(var weight: Double) : QualificationOverrideFactor {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: QualificationOverride, value: Double): Double = value + item.cost * weight
}

data class MinimumQualificationOverrideAdjuster(var minimum: Double) : QualificationOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingQualificationOverrideAdjuster(var step: Double) : QualificationOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationOverride, value: Double): Double = roundTo(value, step)
}

data class TieredQualificationOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationOverrideFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the qualification override steps in order and keeps a trace of each. */
data class QualificationOverrideEngine(
    var steps: MutableList<QualificationOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: QualificationOverride): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationOverrideFactor = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultQualificationOverrideEngine(): QualificationOverrideEngine {
    return QualificationOverrideEngine(
        steps = mutableListOf<QualificationOverrideFactor>(
            QualificationOverrideCostOffsetAdjuster(0.8),
            MinimumQualificationOverrideAdjuster(8.3),
            RoundingQualificationOverrideAdjuster(0.1),
            TieredQualificationOverrideAdjuster(
                mutableListOf<Double>(2548.0, 5905.0, 7680.0, 7687.0),
                mutableListOf<Double>(0.85, 0.99, 0.91, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 55856.5,
    )
}
