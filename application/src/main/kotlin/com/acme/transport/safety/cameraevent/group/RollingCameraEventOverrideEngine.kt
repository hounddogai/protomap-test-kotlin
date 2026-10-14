package com.acme.transport.safety.cameraevent.group

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling camera event override calculation. */
interface RollingCameraEventOverrideStep {
    fun describe(): String
    fun adjust(item: RollingCameraEventOverride, value: Double): Double
}

data class PercentRollingCameraEventOverrideAdjuster(var percent: Double) : RollingCameraEventOverrideStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingCameraEventOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRollingCameraEventOverrideAdjuster(var minimum: Double) : RollingCameraEventOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingCameraEventOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRollingCameraEventOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingCameraEventOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingCameraEventOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RollingCameraEventOverrideRetriesOffsetAdjuster(
    var weight: Double,
) : RollingCameraEventOverrideStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: RollingCameraEventOverride, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class CapRollingCameraEventOverrideAdjuster(var cap: Double) : RollingCameraEventOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingCameraEventOverride, value: Double): Double = minOf(value, cap)
}

data class RoundingRollingCameraEventOverrideAdjuster(var step: Double) : RollingCameraEventOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingCameraEventOverride, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling camera event override steps in order and keeps a trace of each. */
data class RollingCameraEventOverrideEngine(
    var steps: MutableList<RollingCameraEventOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingCameraEventOverride): Double {
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

    fun evaluateAll(items: MutableList<RollingCameraEventOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingCameraEventOverrideEngine(): RollingCameraEventOverrideEngine {
    return RollingCameraEventOverrideEngine(
        steps = mutableListOf<RollingCameraEventOverrideStep>(
            PercentRollingCameraEventOverrideAdjuster(-1.1),
            MinimumRollingCameraEventOverrideAdjuster(38.3),
            TieredRollingCameraEventOverrideAdjuster(
                mutableListOf<Double>(888.0, 1820.0, 5529.0, 6814.0),
                mutableListOf<Double>(0.84, 1.09, 1.06, 0.85),
            ),
            RollingCameraEventOverrideRetriesOffsetAdjuster(1.33),
            CapRollingCameraEventOverrideAdjuster(1379.0),
            RoundingRollingCameraEventOverrideAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 61001.3,
    )
}
