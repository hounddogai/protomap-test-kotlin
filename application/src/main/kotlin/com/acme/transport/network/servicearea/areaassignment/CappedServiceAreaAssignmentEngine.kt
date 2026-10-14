package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped service area assignment calculation. */
interface CappedServiceAreaAssignmentModifier {
    fun describe(): String
    fun adjust(item: CappedServiceAreaAssignment, value: Double): Double
}

data class TieredCappedServiceAreaAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedServiceAreaAssignmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedServiceAreaAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedServiceAreaAssignmentAdjuster(var cap: Double) : CappedServiceAreaAssignmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedServiceAreaAssignment, value: Double): Double = minOf(value, cap)
}

data class CappedServiceAreaAssignmentStopCountOffsetAdjuster(
    var weight: Double,
) : CappedServiceAreaAssignmentModifier {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: CappedServiceAreaAssignment, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class RoundingCappedServiceAreaAssignmentAdjuster(var step: Double) : CappedServiceAreaAssignmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedServiceAreaAssignment, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedServiceAreaAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedServiceAreaAssignmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedServiceAreaAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped service area assignment steps in order and keeps a trace of each. */
data class CappedServiceAreaAssignmentEngine(
    var steps: MutableList<CappedServiceAreaAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedServiceAreaAssignment): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedServiceAreaAssignmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedServiceAreaAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedServiceAreaAssignmentEngine(flags: Registry): CappedServiceAreaAssignmentEngine {
    return CappedServiceAreaAssignmentEngine(
        steps = mutableListOf<CappedServiceAreaAssignmentModifier>(
            TieredCappedServiceAreaAssignmentAdjuster(
                mutableListOf<Double>(1137.0, 1681.0, 7862.0, 8726.0),
                mutableListOf<Double>(1.06, 0.93, 0.92, 0.88),
            ),
            CapCappedServiceAreaAssignmentAdjuster(20435.0),
            CappedServiceAreaAssignmentStopCountOffsetAdjuster(1.32),
            RoundingCappedServiceAreaAssignmentAdjuster(0.25),
            FlaggedCappedServiceAreaAssignmentAdjuster(flags, "capped-service-area-assignment-primary", 0.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 38494.9,
    )
}
