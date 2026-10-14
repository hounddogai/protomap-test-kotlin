package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback incident slot calculation. */
interface FallbackIncidentSlotModifier {
    fun describe(): String
    fun adjust(item: FallbackIncidentSlot, value: Double): Double
}

data class MinimumFallbackIncidentSlotAdjuster(var minimum: Double) : FallbackIncidentSlotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackIncidentSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentFallbackIncidentSlotAdjuster(var percent: Double) : FallbackIncidentSlotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackIncidentSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredFallbackIncidentSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackIncidentSlotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackIncidentSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FallbackIncidentSlotEtaMinutesOffsetAdjuster(var weight: Double) : FallbackIncidentSlotModifier {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: FallbackIncidentSlot, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class RoundingFallbackIncidentSlotAdjuster(var step: Double) : FallbackIncidentSlotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackIncidentSlot, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback incident slot steps in order and keeps a trace of each. */
data class FallbackIncidentSlotEngine(
    var steps: MutableList<FallbackIncidentSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackIncidentSlot): Double {
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

    fun evaluateAll(items: MutableList<FallbackIncidentSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackIncidentSlotEngine(): FallbackIncidentSlotEngine {
    return FallbackIncidentSlotEngine(
        steps = mutableListOf<FallbackIncidentSlotModifier>(
            MinimumFallbackIncidentSlotAdjuster(5.8),
            PercentFallbackIncidentSlotAdjuster(14.5),
            TieredFallbackIncidentSlotAdjuster(
                mutableListOf<Double>(456.0, 7080.0, 7859.0),
                mutableListOf<Double>(1.2, 0.91, 1.03),
            ),
            FallbackIncidentSlotEtaMinutesOffsetAdjuster(0.1),
            RoundingFallbackIncidentSlotAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 4150.9,
    )
}
