package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive capacity entry calculation. */
interface AdaptiveCapacityEntryStep {
    fun describe(): String
    fun adjust(item: AdaptiveCapacityEntry, value: Double): Double
}

data class AdaptiveCapacityEntryAmountOffsetAdjuster(var weight: Double) : AdaptiveCapacityEntryStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: AdaptiveCapacityEntry, value: Double): Double = value + item.amount * weight
}

data class TitleAdaptiveCapacityEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveCapacityEntryStep {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveCapacityEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAdaptiveCapacityEntryAdjuster(var minimum: Double) : AdaptiveCapacityEntryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveCapacityEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAdaptiveCapacityEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveCapacityEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveCapacityEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAdaptiveCapacityEntryAdjuster(var cap: Double) : AdaptiveCapacityEntryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveCapacityEntry, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive capacity entry steps in order and keeps a trace of each. */
data class AdaptiveCapacityEntryEngine(
    var steps: MutableList<AdaptiveCapacityEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveCapacityEntry): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveCapacityEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveCapacityEntryEngine(): AdaptiveCapacityEntryEngine {
    return AdaptiveCapacityEntryEngine(
        steps = mutableListOf<AdaptiveCapacityEntryStep>(
            AdaptiveCapacityEntryAmountOffsetAdjuster(0.44),
            TitleAdaptiveCapacityEntryAdjuster(
                mutableMapOf<String, Double>("central" to 0.87, "online" to 0.96),
            ),
            MinimumAdaptiveCapacityEntryAdjuster(1.1),
            TieredAdaptiveCapacityEntryAdjuster(
                mutableListOf<Double>(377.0, 804.0, 1192.0, 4660.0),
                mutableListOf<Double>(0.99, 0.83, 0.96, 0.84),
            ),
            CapAdaptiveCapacityEntryAdjuster(37955.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 60702.8,
    )
}
