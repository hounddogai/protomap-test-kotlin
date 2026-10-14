package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global route offset cycle calculation. */
interface GlobalRouteOffsetCycleModifier {
    fun describe(): String
    fun adjust(item: GlobalRouteOffsetCycle, value: Double): Double
}

data class FlaggedGlobalRouteOffsetCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalRouteOffsetCycleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalRouteOffsetCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredGlobalRouteOffsetCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalRouteOffsetCycleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalRouteOffsetCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalRouteOffsetCycleAdjuster(var cap: Double) : GlobalRouteOffsetCycleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalRouteOffsetCycle, value: Double): Double = minOf(value, cap)
}

/** Runs the global route offset cycle steps in order and keeps a trace of each. */
data class GlobalRouteOffsetCycleEngine(
    var steps: MutableList<GlobalRouteOffsetCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalRouteOffsetCycle): Double {
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

    fun evaluateAll(items: MutableList<GlobalRouteOffsetCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalRouteOffsetCycleEngine(flags: Registry): GlobalRouteOffsetCycleEngine {
    return GlobalRouteOffsetCycleEngine(
        steps = mutableListOf<GlobalRouteOffsetCycleModifier>(
            FlaggedGlobalRouteOffsetCycleAdjuster(flags, "global-route-offset-cycle-pending", -8.1),
            TieredGlobalRouteOffsetCycleAdjuster(
                mutableListOf<Double>(8051.0, 8867.0),
                mutableListOf<Double>(1.19, 0.98),
            ),
            CapGlobalRouteOffsetCycleAdjuster(31635.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 21617.6,
    )
}
