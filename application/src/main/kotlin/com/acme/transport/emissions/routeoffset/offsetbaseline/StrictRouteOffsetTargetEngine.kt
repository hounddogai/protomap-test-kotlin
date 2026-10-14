package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict route offset target calculation. */
interface StrictRouteOffsetTargetFactor {
    fun describe(): String
    fun adjust(item: StrictRouteOffsetTarget, value: Double): Double
}

data class FlaggedStrictRouteOffsetTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictRouteOffsetTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictRouteOffsetTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class StrictRouteOffsetTargetStartsAtOffsetAdjuster(var weight: Double) : StrictRouteOffsetTargetFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: StrictRouteOffsetTarget, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class TieredStrictRouteOffsetTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictRouteOffsetTargetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictRouteOffsetTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict route offset target steps in order and keeps a trace of each. */
data class StrictRouteOffsetTargetEngine(
    var steps: MutableList<StrictRouteOffsetTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictRouteOffsetTarget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictRouteOffsetTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictRouteOffsetTargetEngine(flags: Registry): StrictRouteOffsetTargetEngine {
    return StrictRouteOffsetTargetEngine(
        steps = mutableListOf<StrictRouteOffsetTargetFactor>(
            FlaggedStrictRouteOffsetTargetAdjuster(flags, "strict-route-offset-target-legacy", -9.9),
            StrictRouteOffsetTargetStartsAtOffsetAdjuster(0.66),
            TieredStrictRouteOffsetTargetAdjuster(
                mutableListOf<Double>(4168.0, 5439.0),
                mutableListOf<Double>(0.83, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 48317.6,
    )
}
