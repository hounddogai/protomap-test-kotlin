package com.acme.transport.lastmile.drop.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback drop tier calculation. */
interface FallbackDropTierFactor {
    fun describe(): String
    fun adjust(item: FallbackDropTier, value: Double): Double
}

data class CapFallbackDropTierAdjuster(var cap: Double) : FallbackDropTierFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDropTier, value: Double): Double = minOf(value, cap)
}

data class FlaggedFallbackDropTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackDropTierFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackDropTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFallbackDropTierAdjuster(var minimum: Double) : FallbackDropTierFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackDropTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback drop tier steps in order and keeps a trace of each. */
data class FallbackDropTierEngine(
    var steps: MutableList<FallbackDropTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackDropTier): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackDropTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackDropTierEngine(flags: Registry): FallbackDropTierEngine {
    return FallbackDropTierEngine(
        steps = mutableListOf<FallbackDropTierFactor>(
            CapFallbackDropTierAdjuster(2308.0),
            FlaggedFallbackDropTierAdjuster(flags, "fallback-drop-tier-strict", -6.6),
            MinimumFallbackDropTierAdjuster(6.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 73217.2,
    )
}
