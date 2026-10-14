package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.kit.flags.Registry

/** One step of the route offset slot calculation. */
interface RouteOffsetSlotFactor {
    fun describe(): String
    fun adjust(item: RouteOffsetSlot, value: Double): Double
}

data class CapRouteOffsetSlotAdjuster(var cap: Double) : RouteOffsetSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetSlot, value: Double): Double = minOf(value, cap)
}

data class FlaggedRouteOffsetSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RouteOffsetSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RouteOffsetSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRouteOffsetSlotAdjuster(var percent: Double) : RouteOffsetSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RouteOffsetSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RouteOffsetSlotThresholdOffsetAdjuster(var weight: Double) : RouteOffsetSlotFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: RouteOffsetSlot, value: Double): Double = value + item.threshold * weight
}

/** Runs the route offset slot steps in order and keeps a trace of each. */
data class RouteOffsetSlotEngine(
    var steps: MutableList<RouteOffsetSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RouteOffsetSlot): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RouteOffsetSlotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RouteOffsetSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRouteOffsetSlotEngine(flags: Registry): RouteOffsetSlotEngine {
    return RouteOffsetSlotEngine(
        steps = mutableListOf<RouteOffsetSlotFactor>(
            CapRouteOffsetSlotAdjuster(29913.0),
            FlaggedRouteOffsetSlotAdjuster(flags, "route-offset-slot-lenient", 4.7),
            PercentRouteOffsetSlotAdjuster(-18.1),
            RouteOffsetSlotThresholdOffsetAdjuster(1.99),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 7105.0,
    )
}
