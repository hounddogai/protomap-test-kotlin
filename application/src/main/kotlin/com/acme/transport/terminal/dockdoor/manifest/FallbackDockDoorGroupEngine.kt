package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback dock door group calculation. */
interface FallbackDockDoorGroupStep {
    fun describe(): String
    fun adjust(item: FallbackDockDoorGroup, value: Double): Double
}

data class TieredFallbackDockDoorGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackDockDoorGroupStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackDockDoorGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedFallbackDockDoorGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackDockDoorGroupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackDockDoorGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackDockDoorGroupAdjuster(var cap: Double) : FallbackDockDoorGroupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDockDoorGroup, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback dock door group steps in order and keeps a trace of each. */
data class FallbackDockDoorGroupEngine(
    var steps: MutableList<FallbackDockDoorGroupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackDockDoorGroup): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackDockDoorGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackDockDoorGroupEngine(flags: Registry): FallbackDockDoorGroupEngine {
    return FallbackDockDoorGroupEngine(
        steps = mutableListOf<FallbackDockDoorGroupStep>(
            TieredFallbackDockDoorGroupAdjuster(
                mutableListOf<Double>(74.0, 3263.0, 6252.0, 7087.0),
                mutableListOf<Double>(1.04, 1.06, 0.98, 1.0),
            ),
            FlaggedFallbackDockDoorGroupAdjuster(flags, "fallback-dock-door-group-regional", -0.1),
            CapFallbackDockDoorGroupAdjuster(26069.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 94260.2,
    )
}
