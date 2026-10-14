package com.acme.transport.safety.incident.incidentproposal

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient incident adjustment calculation. */
interface LenientIncidentAdjustmentModifier {
    fun describe(): String
    fun adjust(item: LenientIncidentAdjustment, value: Double): Double
}

data class RoundingLenientIncidentAdjustmentAdjuster(var step: Double) : LenientIncidentAdjustmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientIncidentAdjustment, value: Double): Double = roundTo(value, step)
}

data class TieredLenientIncidentAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientIncidentAdjustmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientIncidentAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLenientIncidentAdjustmentAdjuster(var minimum: Double) : LenientIncidentAdjustmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientIncidentAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLenientIncidentAdjustmentAdjuster(var percent: Double) : LenientIncidentAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientIncidentAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LenientIncidentAdjustmentFloorValueOffsetAdjuster(
    var weight: Double,
) : LenientIncidentAdjustmentModifier {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: LenientIncidentAdjustment, value: Double): Double = value + item.floorValue * weight
}

/** Runs the lenient incident adjustment steps in order and keeps a trace of each. */
data class LenientIncidentAdjustmentEngine(
    var steps: MutableList<LenientIncidentAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientIncidentAdjustment): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientIncidentAdjustmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LenientIncidentAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientIncidentAdjustmentEngine(): LenientIncidentAdjustmentEngine {
    return LenientIncidentAdjustmentEngine(
        steps = mutableListOf<LenientIncidentAdjustmentModifier>(
            RoundingLenientIncidentAdjustmentAdjuster(0.1),
            TieredLenientIncidentAdjustmentAdjuster(
                mutableListOf<Double>(2483.0, 5263.0, 5541.0, 6182.0),
                mutableListOf<Double>(1.05, 1.16, 1.06, 1.04),
            ),
            MinimumLenientIncidentAdjustmentAdjuster(31.8),
            PercentLenientIncidentAdjustmentAdjuster(11.0),
            LenientIncidentAdjustmentFloorValueOffsetAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 77708.6,
    )
}
