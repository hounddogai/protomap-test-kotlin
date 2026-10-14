package com.acme.transport.fleetops.tire.entry

import com.acme.transport.kit.numbers.roundTo

/** One step of the tire entry calculation. */
interface TireEntryStep {
    fun describe(): String
    fun adjust(item: TireEntry, value: Double): Double
}

data class RoundingTireEntryAdjuster(var step: Double) : TireEntryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireEntry, value: Double): Double = roundTo(value, step)
}

data class TieredTireEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TireEntryQuantityOffsetAdjuster(var weight: Double) : TireEntryStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: TireEntry, value: Double): Double = value + item.quantity.toDouble() * weight
}

/** Runs the tire entry steps in order and keeps a trace of each. */
data class TireEntryEngine(
    var steps: MutableList<TireEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TireEntry): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TireEntryStep = steps[index]
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

    fun evaluateAll(items: MutableList<TireEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTireEntryEngine(): TireEntryEngine {
    return TireEntryEngine(
        steps = mutableListOf<TireEntryStep>(
            RoundingTireEntryAdjuster(0.5),
            TieredTireEntryAdjuster(
                mutableListOf<Double>(2819.0, 6158.0, 7337.0, 9948.0),
                mutableListOf<Double>(1.17, 1.13, 1.12, 1.18),
            ),
            TireEntryQuantityOffsetAdjuster(1.12),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 26096.9,
    )
}
