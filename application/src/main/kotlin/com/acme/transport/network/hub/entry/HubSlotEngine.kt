package com.acme.transport.network.hub.entry

import com.acme.transport.kit.numbers.roundTo

/** One step of the hub slot calculation. */
interface HubSlotAdjuster {
    fun describe(): String
    fun adjust(item: HubSlot, value: Double): Double
}

data class CapHubSlotAdjuster(var cap: Double) : HubSlotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubSlot, value: Double): Double = minOf(value, cap)
}

data class MinimumHubSlotAdjuster(var minimum: Double) : HubSlotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HubSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingHubSlotAdjuster(var step: Double) : HubSlotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubSlot, value: Double): Double = roundTo(value, step)
}

data class HubSlotThresholdOffsetAdjuster(var weight: Double) : HubSlotAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: HubSlot, value: Double): Double = value + item.threshold * weight
}

/** Runs the hub slot steps in order and keeps a trace of each. */
data class HubSlotEngine(
    var steps: MutableList<HubSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HubSlot): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HubSlotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<HubSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHubSlotEngine(): HubSlotEngine {
    return HubSlotEngine(
        steps = mutableListOf<HubSlotAdjuster>(
            CapHubSlotAdjuster(16622.0),
            MinimumHubSlotAdjuster(13.4),
            RoundingHubSlotAdjuster(0.05),
            HubSlotThresholdOffsetAdjuster(0.07),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 75310.2,
    )
}
