package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled route offset mapping calculation. */
interface ScheduledRouteOffsetMappingModifier {
    fun describe(): String
    fun adjust(item: ScheduledRouteOffsetMapping, value: Double): Double
}

data class CategoryScheduledRouteOffsetMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledRouteOffsetMappingModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: ScheduledRouteOffsetMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedScheduledRouteOffsetMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledRouteOffsetMappingModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledRouteOffsetMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScheduledRouteOffsetMappingAdjuster(
    var percent: Double,
) : ScheduledRouteOffsetMappingModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledRouteOffsetMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumScheduledRouteOffsetMappingAdjuster(
    var minimum: Double,
) : ScheduledRouteOffsetMappingModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledRouteOffsetMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled route offset mapping steps in order and keeps a trace of each. */
data class ScheduledRouteOffsetMappingEngine(
    var steps: MutableList<ScheduledRouteOffsetMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledRouteOffsetMapping): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledRouteOffsetMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledRouteOffsetMappingEngine(flags: Registry): ScheduledRouteOffsetMappingEngine {
    return ScheduledRouteOffsetMappingEngine(
        steps = mutableListOf<ScheduledRouteOffsetMappingModifier>(
            CategoryScheduledRouteOffsetMappingAdjuster(
                mutableMapOf<String, Double>("west" to 0.87, "east" to 1.02, "south" to 1.07),
            ),
            FlaggedScheduledRouteOffsetMappingAdjuster(flags, "scheduled-route-offset-mapping-tiered", -4.7),
            PercentScheduledRouteOffsetMappingAdjuster(-4.7),
            MinimumScheduledRouteOffsetMappingAdjuster(13.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 48154.9,
    )
}
