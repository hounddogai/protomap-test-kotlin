package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual route offset baseline calculation. */
interface ManualRouteOffsetBaselineFactor {
    fun describe(): String
    fun adjust(item: ManualRouteOffsetBaseline, value: Double): Double
}

data class CapManualRouteOffsetBaselineAdjuster(var cap: Double) : ManualRouteOffsetBaselineFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualRouteOffsetBaseline, value: Double): Double = minOf(value, cap)
}

data class MinimumManualRouteOffsetBaselineAdjuster(var minimum: Double) : ManualRouteOffsetBaselineFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualRouteOffsetBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ManualRouteOffsetBaselineMinimumOffsetAdjuster(
    var weight: Double,
) : ManualRouteOffsetBaselineFactor {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: ManualRouteOffsetBaseline, value: Double): Double = value + item.minimum * weight
}

data class FlaggedManualRouteOffsetBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualRouteOffsetBaselineFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualRouteOffsetBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingManualRouteOffsetBaselineAdjuster(var step: Double) : ManualRouteOffsetBaselineFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualRouteOffsetBaseline, value: Double): Double = roundTo(value, step)
}

/** Runs the manual route offset baseline steps in order and keeps a trace of each. */
data class ManualRouteOffsetBaselineEngine(
    var steps: MutableList<ManualRouteOffsetBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualRouteOffsetBaseline): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualRouteOffsetBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualRouteOffsetBaselineEngine(flags: Registry): ManualRouteOffsetBaselineEngine {
    return ManualRouteOffsetBaselineEngine(
        steps = mutableListOf<ManualRouteOffsetBaselineFactor>(
            CapManualRouteOffsetBaselineAdjuster(49554.0),
            MinimumManualRouteOffsetBaselineAdjuster(2.0),
            ManualRouteOffsetBaselineMinimumOffsetAdjuster(0.23),
            FlaggedManualRouteOffsetBaselineAdjuster(flags, "manual-route-offset-baseline-regional", 0.6),
            RoundingManualRouteOffsetBaselineAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 60481.4,
    )
}
