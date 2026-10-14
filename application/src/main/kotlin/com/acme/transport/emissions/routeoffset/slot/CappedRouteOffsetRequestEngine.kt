package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.kit.flags.Registry

/** One step of the capped route offset request calculation. */
interface CappedRouteOffsetRequestModifier {
    fun describe(): String
    fun adjust(item: CappedRouteOffsetRequest, value: Double): Double
}

data class MinimumCappedRouteOffsetRequestAdjuster(var minimum: Double) : CappedRouteOffsetRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedRouteOffsetRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CappedRouteOffsetRequestFloorValueOffsetAdjuster(
    var weight: Double,
) : CappedRouteOffsetRequestModifier {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: CappedRouteOffsetRequest, value: Double): Double = value + item.floorValue * weight
}

data class PercentCappedRouteOffsetRequestAdjuster(var percent: Double) : CappedRouteOffsetRequestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedRouteOffsetRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCappedRouteOffsetRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedRouteOffsetRequestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedRouteOffsetRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped route offset request steps in order and keeps a trace of each. */
data class CappedRouteOffsetRequestEngine(
    var steps: MutableList<CappedRouteOffsetRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedRouteOffsetRequest): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedRouteOffsetRequestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedRouteOffsetRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedRouteOffsetRequestEngine(flags: Registry): CappedRouteOffsetRequestEngine {
    return CappedRouteOffsetRequestEngine(
        steps = mutableListOf<CappedRouteOffsetRequestModifier>(
            MinimumCappedRouteOffsetRequestAdjuster(7.0),
            CappedRouteOffsetRequestFloorValueOffsetAdjuster(1.57),
            PercentCappedRouteOffsetRequestAdjuster(19.3),
            FlaggedCappedRouteOffsetRequestAdjuster(flags, "capped-route-offset-request-scheduled", 6.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 39182.9,
    )
}
