package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback appointment rollup calculation. */
interface FallbackAppointmentRollupStep {
    fun describe(): String
    fun adjust(item: FallbackAppointmentRollup, value: Double): Double
}

data class MinimumFallbackAppointmentRollupAdjuster(var minimum: Double) : FallbackAppointmentRollupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackAppointmentRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FallbackAppointmentRollupDiscountOffsetAdjuster(var weight: Double) : FallbackAppointmentRollupStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: FallbackAppointmentRollup, value: Double): Double = value + item.discount * weight
}

data class CapFallbackAppointmentRollupAdjuster(var cap: Double) : FallbackAppointmentRollupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackAppointmentRollup, value: Double): Double = minOf(value, cap)
}

data class RoundingFallbackAppointmentRollupAdjuster(var step: Double) : FallbackAppointmentRollupStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackAppointmentRollup, value: Double): Double = roundTo(value, step)
}

data class TieredFallbackAppointmentRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackAppointmentRollupStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackAppointmentRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback appointment rollup steps in order and keeps a trace of each. */
data class FallbackAppointmentRollupEngine(
    var steps: MutableList<FallbackAppointmentRollupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackAppointmentRollup): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackAppointmentRollupStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackAppointmentRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackAppointmentRollupEngine(): FallbackAppointmentRollupEngine {
    return FallbackAppointmentRollupEngine(
        steps = mutableListOf<FallbackAppointmentRollupStep>(
            MinimumFallbackAppointmentRollupAdjuster(45.0),
            FallbackAppointmentRollupDiscountOffsetAdjuster(1.5),
            CapFallbackAppointmentRollupAdjuster(43496.0),
            RoundingFallbackAppointmentRollupAdjuster(0.05),
            TieredFallbackAppointmentRollupAdjuster(
                mutableListOf<Double>(2564.0, 3756.0, 4472.0),
                mutableListOf<Double>(0.97, 1.04, 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 89882.4,
    )
}
