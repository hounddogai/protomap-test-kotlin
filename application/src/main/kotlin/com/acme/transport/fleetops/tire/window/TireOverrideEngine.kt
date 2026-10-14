package com.acme.transport.fleetops.tire.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tire override calculation. */
interface TireOverrideStep {
    fun describe(): String
    fun adjust(item: TireOverride, value: Double): Double
}

data class TireOverrideStopCountOffsetAdjuster(var weight: Double) : TireOverrideStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: TireOverride, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class FlaggedTireOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTireOverrideAdjuster(var step: Double) : TireOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireOverride, value: Double): Double = roundTo(value, step)
}

/** Runs the tire override steps in order and keeps a trace of each. */
data class TireOverrideEngine(
    var steps: MutableList<TireOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TireOverride): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TireOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTireOverrideEngine(flags: Registry): TireOverrideEngine {
    return TireOverrideEngine(
        steps = mutableListOf<TireOverrideStep>(
            TireOverrideStopCountOffsetAdjuster(0.81),
            FlaggedTireOverrideAdjuster(flags, "tire-override-scheduled", 6.9),
            RoundingTireOverrideAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 59142.2,
    )
}
