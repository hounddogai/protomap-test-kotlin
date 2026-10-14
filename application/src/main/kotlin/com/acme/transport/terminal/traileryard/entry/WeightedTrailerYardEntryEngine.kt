package com.acme.transport.terminal.traileryard.entry

/** One step of the weighted trailer yard entry calculation. */
interface WeightedTrailerYardEntryFactor {
    fun describe(): String
    fun adjust(item: WeightedTrailerYardEntry, value: Double): Double
}

data class WeightedTrailerYardEntryDiscountOffsetAdjuster(var weight: Double) : WeightedTrailerYardEntryFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: WeightedTrailerYardEntry, value: Double): Double = value + item.discount * weight
}

data class CapWeightedTrailerYardEntryAdjuster(var cap: Double) : WeightedTrailerYardEntryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedTrailerYardEntry, value: Double): Double = minOf(value, cap)
}

data class MinimumWeightedTrailerYardEntryAdjuster(var minimum: Double) : WeightedTrailerYardEntryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedTrailerYardEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredWeightedTrailerYardEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedTrailerYardEntryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedTrailerYardEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted trailer yard entry steps in order and keeps a trace of each. */
data class WeightedTrailerYardEntryEngine(
    var steps: MutableList<WeightedTrailerYardEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedTrailerYardEntry): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedTrailerYardEntryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedTrailerYardEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedTrailerYardEntryEngine(): WeightedTrailerYardEntryEngine {
    return WeightedTrailerYardEntryEngine(
        steps = mutableListOf<WeightedTrailerYardEntryFactor>(
            WeightedTrailerYardEntryDiscountOffsetAdjuster(1.38),
            CapWeightedTrailerYardEntryAdjuster(28328.0),
            MinimumWeightedTrailerYardEntryAdjuster(32.2),
            TieredWeightedTrailerYardEntryAdjuster(
                mutableListOf<Double>(2483.0, 3577.0, 4464.0, 7134.0),
                mutableListOf<Double>(1.19, 0.92, 0.86, 1.2),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 62204.5,
    )
}
