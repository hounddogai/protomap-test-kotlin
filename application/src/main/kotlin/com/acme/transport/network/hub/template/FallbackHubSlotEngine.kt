package com.acme.transport.network.hub.template

import com.acme.transport.kit.flags.Registry

/** One step of the fallback hub slot calculation. */
interface FallbackHubSlotAdjuster {
    fun describe(): String
    fun adjust(item: FallbackHubSlot, value: Double): Double
}

data class TieredFallbackHubSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackHubSlotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackHubSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameFallbackHubSlotAdjuster(var factors: MutableMap<String, Double>) : FallbackHubSlotAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: FallbackHubSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapFallbackHubSlotAdjuster(var cap: Double) : FallbackHubSlotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackHubSlot, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackHubSlotAdjuster(var percent: Double) : FallbackHubSlotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackHubSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedFallbackHubSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackHubSlotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackHubSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback hub slot steps in order and keeps a trace of each. */
data class FallbackHubSlotEngine(
    var steps: MutableList<FallbackHubSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackHubSlot): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackHubSlotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackHubSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackHubSlotEngine(flags: Registry): FallbackHubSlotEngine {
    return FallbackHubSlotEngine(
        steps = mutableListOf<FallbackHubSlotAdjuster>(
            TieredFallbackHubSlotAdjuster(
                mutableListOf<Double>(2372.0, 5079.0, 9616.0),
                mutableListOf<Double>(0.82, 1.11, 1.08),
            ),
            NameFallbackHubSlotAdjuster(mutableMapOf<String, Double>("south" to 1.33)),
            CapFallbackHubSlotAdjuster(32963.0),
            PercentFallbackHubSlotAdjuster(22.0),
            FlaggedFallbackHubSlotAdjuster(flags, "fallback-hub-slot-adaptive", -7.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 70589.0,
    )
}
