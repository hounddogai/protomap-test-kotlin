package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted route offset slot calculation. */
interface WeightedRouteOffsetSlotModifier {
    fun describe(): String
    fun adjust(item: WeightedRouteOffsetSlot, value: Double): Double
}

data class RoundingWeightedRouteOffsetSlotAdjuster(var step: Double) : WeightedRouteOffsetSlotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedRouteOffsetSlot, value: Double): Double = roundTo(value, step)
}

data class MinimumWeightedRouteOffsetSlotAdjuster(var minimum: Double) : WeightedRouteOffsetSlotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedRouteOffsetSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class WeightedRouteOffsetSlotFloorValueOffsetAdjuster(
    var weight: Double,
) : WeightedRouteOffsetSlotModifier {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: WeightedRouteOffsetSlot, value: Double): Double = value + item.floorValue * weight
}

data class FlaggedWeightedRouteOffsetSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedRouteOffsetSlotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedRouteOffsetSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted route offset slot steps in order and keeps a trace of each. */
data class WeightedRouteOffsetSlotEngine(
    var steps: MutableList<WeightedRouteOffsetSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedRouteOffsetSlot): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedRouteOffsetSlotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedRouteOffsetSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedRouteOffsetSlotEngine(flags: Registry): WeightedRouteOffsetSlotEngine {
    return WeightedRouteOffsetSlotEngine(
        steps = mutableListOf<WeightedRouteOffsetSlotModifier>(
            RoundingWeightedRouteOffsetSlotAdjuster(5.0),
            MinimumWeightedRouteOffsetSlotAdjuster(48.0),
            WeightedRouteOffsetSlotFloorValueOffsetAdjuster(0.83),
            FlaggedWeightedRouteOffsetSlotAdjuster(flags, "weighted-route-offset-slot-primary", 0.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 71540.4,
    )
}
