package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback zone slot calculation. */
interface FallbackZoneSlotModifier {
    fun describe(): String
    fun adjust(item: FallbackZoneSlot, value: Double): Double
}

data class CountryFallbackZoneSlotAdjuster(var factors: MutableMap<String, Double>) : FallbackZoneSlotModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: FallbackZoneSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentFallbackZoneSlotAdjuster(var percent: Double) : FallbackZoneSlotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackZoneSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFallbackZoneSlotAdjuster(var cap: Double) : FallbackZoneSlotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackZoneSlot, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback zone slot steps in order and keeps a trace of each. */
data class FallbackZoneSlotEngine(
    var steps: MutableList<FallbackZoneSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackZoneSlot): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackZoneSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackZoneSlotEngine(): FallbackZoneSlotEngine {
    return FallbackZoneSlotEngine(
        steps = mutableListOf<FallbackZoneSlotModifier>(
            CountryFallbackZoneSlotAdjuster(mutableMapOf<String, Double>("online" to 1.08)),
            PercentFallbackZoneSlotAdjuster(-1.4),
            CapFallbackZoneSlotAdjuster(41511.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 34416.0,
    )
}
