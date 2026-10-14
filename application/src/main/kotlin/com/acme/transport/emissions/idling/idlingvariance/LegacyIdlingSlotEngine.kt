package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy idling slot calculation. */
interface LegacyIdlingSlotAdjuster {
    fun describe(): String
    fun adjust(item: LegacyIdlingSlot, value: Double): Double
}

data class MinimumLegacyIdlingSlotAdjuster(var minimum: Double) : LegacyIdlingSlotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyIdlingSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLegacyIdlingSlotAdjuster(var percent: Double) : LegacyIdlingSlotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyIdlingSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLegacyIdlingSlotAdjuster(var cap: Double) : LegacyIdlingSlotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyIdlingSlot, value: Double): Double = minOf(value, cap)
}

data class RoundingLegacyIdlingSlotAdjuster(var step: Double) : LegacyIdlingSlotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyIdlingSlot, value: Double): Double = roundTo(value, step)
}

data class SegmentLegacyIdlingSlotAdjuster(var factors: MutableMap<String, Double>) : LegacyIdlingSlotAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: LegacyIdlingSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLegacyIdlingSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyIdlingSlotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyIdlingSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy idling slot steps in order and keeps a trace of each. */
data class LegacyIdlingSlotEngine(
    var steps: MutableList<LegacyIdlingSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyIdlingSlot): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyIdlingSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyIdlingSlotEngine(): LegacyIdlingSlotEngine {
    return LegacyIdlingSlotEngine(
        steps = mutableListOf<LegacyIdlingSlotAdjuster>(
            MinimumLegacyIdlingSlotAdjuster(19.3),
            PercentLegacyIdlingSlotAdjuster(13.5),
            CapLegacyIdlingSlotAdjuster(15024.0),
            RoundingLegacyIdlingSlotAdjuster(0.05),
            SegmentLegacyIdlingSlotAdjuster(mutableMapOf<String, Double>("central" to 0.74, "retail" to 1.3)),
            TieredLegacyIdlingSlotAdjuster(
                mutableListOf<Double>(917.0, 1433.0, 4698.0, 9341.0),
                mutableListOf<Double>(0.88, 0.85, 0.96, 1.07),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 9926.3,
    )
}
