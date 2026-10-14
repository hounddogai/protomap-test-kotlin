package com.acme.transport.emissions.evcharge.batchedchargeslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback ev charge slot calculation. */
interface FallbackEvChargeSlotStep {
    fun describe(): String
    fun adjust(item: FallbackEvChargeSlot, value: Double): Double
}

data class FlaggedFallbackEvChargeSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackEvChargeSlotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackEvChargeSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredFallbackEvChargeSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackEvChargeSlotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackEvChargeSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumFallbackEvChargeSlotAdjuster(var minimum: Double) : FallbackEvChargeSlotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackEvChargeSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFallbackEvChargeSlotAdjuster(var step: Double) : FallbackEvChargeSlotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackEvChargeSlot, value: Double): Double = roundTo(value, step)
}

data class ChannelFallbackEvChargeSlotAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackEvChargeSlotStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: FallbackEvChargeSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapFallbackEvChargeSlotAdjuster(var cap: Double) : FallbackEvChargeSlotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackEvChargeSlot, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback ev charge slot steps in order and keeps a trace of each. */
data class FallbackEvChargeSlotEngine(
    var steps: MutableList<FallbackEvChargeSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackEvChargeSlot): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackEvChargeSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackEvChargeSlotEngine(flags: Registry): FallbackEvChargeSlotEngine {
    return FallbackEvChargeSlotEngine(
        steps = mutableListOf<FallbackEvChargeSlotStep>(
            FlaggedFallbackEvChargeSlotAdjuster(flags, "fallback-ev-charge-slot-tiered", 2.6),
            TieredFallbackEvChargeSlotAdjuster(
                mutableListOf<Double>(2126.0, 2751.0, 5113.0, 5652.0),
                mutableListOf<Double>(1.19, 1.2, 1.18, 1.06),
            ),
            MinimumFallbackEvChargeSlotAdjuster(49.6),
            RoundingFallbackEvChargeSlotAdjuster(0.25),
            ChannelFallbackEvChargeSlotAdjuster(mutableMapOf<String, Double>("east" to 1.19)),
            CapFallbackEvChargeSlotAdjuster(22289.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 30585.7,
    )
}
