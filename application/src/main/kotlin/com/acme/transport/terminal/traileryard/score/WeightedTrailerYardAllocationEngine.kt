package com.acme.transport.terminal.traileryard.score

/** One step of the weighted trailer yard allocation calculation. */
interface WeightedTrailerYardAllocationModifier {
    fun describe(): String
    fun adjust(item: WeightedTrailerYardAllocation, value: Double): Double
}

data class WeightedTrailerYardAllocationEtaMinutesOffsetAdjuster(
    var weight: Double,
) : WeightedTrailerYardAllocationModifier {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: WeightedTrailerYardAllocation, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class CapWeightedTrailerYardAllocationAdjuster(var cap: Double) : WeightedTrailerYardAllocationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedTrailerYardAllocation, value: Double): Double = minOf(value, cap)
}

data class TieredWeightedTrailerYardAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedTrailerYardAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedTrailerYardAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted trailer yard allocation steps in order and keeps a trace of each. */
data class WeightedTrailerYardAllocationEngine(
    var steps: MutableList<WeightedTrailerYardAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedTrailerYardAllocation): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedTrailerYardAllocationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedTrailerYardAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedTrailerYardAllocationEngine(): WeightedTrailerYardAllocationEngine {
    return WeightedTrailerYardAllocationEngine(
        steps = mutableListOf<WeightedTrailerYardAllocationModifier>(
            WeightedTrailerYardAllocationEtaMinutesOffsetAdjuster(0.8),
            CapWeightedTrailerYardAllocationAdjuster(25005.0),
            TieredWeightedTrailerYardAllocationAdjuster(
                mutableListOf<Double>(1699.0, 5373.0),
                mutableListOf<Double>(0.85, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 82462.9,
    )
}
