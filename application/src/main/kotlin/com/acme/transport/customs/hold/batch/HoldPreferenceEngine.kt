package com.acme.transport.customs.hold.batch

import com.acme.transport.kit.numbers.roundTo

/** One step of the hold preference calculation. */
interface HoldPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: HoldPreference, value: Double): Double
}

data class RoundingHoldPreferenceAdjuster(var step: Double) : HoldPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HoldPreference, value: Double): Double = roundTo(value, step)
}

data class TieredHoldPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HoldPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HoldPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class HoldPreferenceFuelLitersOffsetAdjuster(var weight: Double) : HoldPreferenceAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: HoldPreference, value: Double): Double = value + item.fuelLiters * weight
}

/** Runs the hold preference steps in order and keeps a trace of each. */
data class HoldPreferenceEngine(
    var steps: MutableList<HoldPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HoldPreference): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HoldPreferenceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<HoldPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHoldPreferenceEngine(): HoldPreferenceEngine {
    return HoldPreferenceEngine(
        steps = mutableListOf<HoldPreferenceAdjuster>(
            RoundingHoldPreferenceAdjuster(0.25),
            TieredHoldPreferenceAdjuster(
                mutableListOf<Double>(1167.0, 3832.0, 7623.0),
                mutableListOf<Double>(0.96, 1.12, 0.83),
            ),
            HoldPreferenceFuelLitersOffsetAdjuster(1.41),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 3351.7,
    )
}
