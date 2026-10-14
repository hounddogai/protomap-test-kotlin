package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary accessorial entry calculation. */
interface PrimaryAccessorialEntryStep {
    fun describe(): String
    fun adjust(item: PrimaryAccessorialEntry, value: Double): Double
}

data class RoundingPrimaryAccessorialEntryAdjuster(var step: Double) : PrimaryAccessorialEntryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryAccessorialEntry, value: Double): Double = roundTo(value, step)
}

data class TieredPrimaryAccessorialEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryAccessorialEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryAccessorialEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PrimaryAccessorialEntryDistanceKmOffsetAdjuster(var weight: Double) : PrimaryAccessorialEntryStep {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: PrimaryAccessorialEntry, value: Double): Double = value + item.distanceKm * weight
}

data class MinimumPrimaryAccessorialEntryAdjuster(var minimum: Double) : PrimaryAccessorialEntryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryAccessorialEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPrimaryAccessorialEntryAdjuster(var cap: Double) : PrimaryAccessorialEntryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryAccessorialEntry, value: Double): Double = minOf(value, cap)
}

/** Runs the primary accessorial entry steps in order and keeps a trace of each. */
data class PrimaryAccessorialEntryEngine(
    var steps: MutableList<PrimaryAccessorialEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryAccessorialEntry): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryAccessorialEntryStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryAccessorialEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryAccessorialEntryEngine(): PrimaryAccessorialEntryEngine {
    return PrimaryAccessorialEntryEngine(
        steps = mutableListOf<PrimaryAccessorialEntryStep>(
            RoundingPrimaryAccessorialEntryAdjuster(1.0),
            TieredPrimaryAccessorialEntryAdjuster(
                mutableListOf<Double>(794.0, 2004.0),
                mutableListOf<Double>(1.01, 1.09),
            ),
            PrimaryAccessorialEntryDistanceKmOffsetAdjuster(0.1),
            MinimumPrimaryAccessorialEntryAdjuster(34.8),
            CapPrimaryAccessorialEntryAdjuster(49707.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 94122.8,
    )
}
