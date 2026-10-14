package com.acme.transport.customs.tariff.variance

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global tariff slot calculation. */
interface GlobalTariffSlotFactor {
    fun describe(): String
    fun adjust(item: GlobalTariffSlot, value: Double): Double
}

data class MinimumGlobalTariffSlotAdjuster(var minimum: Double) : GlobalTariffSlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalTariffSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapGlobalTariffSlotAdjuster(var cap: Double) : GlobalTariffSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalTariffSlot, value: Double): Double = minOf(value, cap)
}

data class RoundingGlobalTariffSlotAdjuster(var step: Double) : GlobalTariffSlotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalTariffSlot, value: Double): Double = roundTo(value, step)
}

/** Runs the global tariff slot steps in order and keeps a trace of each. */
data class GlobalTariffSlotEngine(
    var steps: MutableList<GlobalTariffSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalTariffSlot): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalTariffSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalTariffSlotEngine(): GlobalTariffSlotEngine {
    return GlobalTariffSlotEngine(
        steps = mutableListOf<GlobalTariffSlotFactor>(
            MinimumGlobalTariffSlotAdjuster(14.6),
            CapGlobalTariffSlotAdjuster(47387.0),
            RoundingGlobalTariffSlotAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 93939.4,
    )
}
