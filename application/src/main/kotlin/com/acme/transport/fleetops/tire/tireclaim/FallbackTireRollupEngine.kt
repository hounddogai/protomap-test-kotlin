package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback tire rollup calculation. */
interface FallbackTireRollupStep {
    fun describe(): String
    fun adjust(item: FallbackTireRollup, value: Double): Double
}

data class FlaggedFallbackTireRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackTireRollupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackTireRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackTireRollupAdjuster(var cap: Double) : FallbackTireRollupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackTireRollup, value: Double): Double = minOf(value, cap)
}

data class MinimumFallbackTireRollupAdjuster(var minimum: Double) : FallbackTireRollupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackTireRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback tire rollup steps in order and keeps a trace of each. */
data class FallbackTireRollupEngine(
    var steps: MutableList<FallbackTireRollupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackTireRollup): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackTireRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackTireRollupEngine(flags: Registry): FallbackTireRollupEngine {
    return FallbackTireRollupEngine(
        steps = mutableListOf<FallbackTireRollupStep>(
            FlaggedFallbackTireRollupAdjuster(flags, "fallback-tire-rollup-strict", -8.7),
            CapFallbackTireRollupAdjuster(44655.0),
            MinimumFallbackTireRollupAdjuster(19.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 41018.8,
    )
}
