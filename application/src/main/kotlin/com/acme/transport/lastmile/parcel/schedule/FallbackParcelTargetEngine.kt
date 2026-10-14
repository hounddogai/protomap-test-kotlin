package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.kit.flags.Registry

/** One step of the fallback parcel target calculation. */
interface FallbackParcelTargetFactor {
    fun describe(): String
    fun adjust(item: FallbackParcelTarget, value: Double): Double
}

data class FlaggedFallbackParcelTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackParcelTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackParcelTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFallbackParcelTargetAdjuster(var minimum: Double) : FallbackParcelTargetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackParcelTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentFallbackParcelTargetAdjuster(var percent: Double) : FallbackParcelTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackParcelTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback parcel target steps in order and keeps a trace of each. */
data class FallbackParcelTargetEngine(
    var steps: MutableList<FallbackParcelTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackParcelTarget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackParcelTargetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackParcelTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackParcelTargetEngine(flags: Registry): FallbackParcelTargetEngine {
    return FallbackParcelTargetEngine(
        steps = mutableListOf<FallbackParcelTargetFactor>(
            FlaggedFallbackParcelTargetAdjuster(flags, "fallback-parcel-target-adaptive", -4.0),
            MinimumFallbackParcelTargetAdjuster(1.9),
            PercentFallbackParcelTargetAdjuster(17.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 87989.5,
    )
}
