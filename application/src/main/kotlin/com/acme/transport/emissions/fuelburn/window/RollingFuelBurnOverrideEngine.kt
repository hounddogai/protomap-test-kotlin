package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling fuel burn override calculation. */
interface RollingFuelBurnOverrideStep {
    fun describe(): String
    fun adjust(item: RollingFuelBurnOverride, value: Double): Double
}

data class MinimumRollingFuelBurnOverrideAdjuster(var minimum: Double) : RollingFuelBurnOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingFuelBurnOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRollingFuelBurnOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingFuelBurnOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingFuelBurnOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRollingFuelBurnOverrideAdjuster(var cap: Double) : RollingFuelBurnOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingFuelBurnOverride, value: Double): Double = minOf(value, cap)
}

data class RollingFuelBurnOverrideOdometerOffsetAdjuster(var weight: Double) : RollingFuelBurnOverrideStep {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: RollingFuelBurnOverride, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

/** Runs the rolling fuel burn override steps in order and keeps a trace of each. */
data class RollingFuelBurnOverrideEngine(
    var steps: MutableList<RollingFuelBurnOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingFuelBurnOverride): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingFuelBurnOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingFuelBurnOverrideEngine(flags: Registry): RollingFuelBurnOverrideEngine {
    return RollingFuelBurnOverrideEngine(
        steps = mutableListOf<RollingFuelBurnOverrideStep>(
            MinimumRollingFuelBurnOverrideAdjuster(48.3),
            FlaggedRollingFuelBurnOverrideAdjuster(flags, "rolling-fuel-burn-override-seasonal", 0.3),
            CapRollingFuelBurnOverrideAdjuster(31448.0),
            RollingFuelBurnOverrideOdometerOffsetAdjuster(1.55),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 52964.3,
    )
}
