package com.acme.transport.fleetops.tire.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling tire target calculation. */
interface RollingTireTargetStep {
    fun describe(): String
    fun adjust(item: RollingTireTarget, value: Double): Double
}

data class RollingTireTargetFloorValueOffsetAdjuster(var weight: Double) : RollingTireTargetStep {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: RollingTireTarget, value: Double): Double = value + item.floorValue * weight
}

data class PercentRollingTireTargetAdjuster(var percent: Double) : RollingTireTargetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingTireTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRollingTireTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingTireTargetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingTireTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRollingTireTargetAdjuster(var step: Double) : RollingTireTargetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingTireTarget, value: Double): Double = roundTo(value, step)
}

data class TieredRollingTireTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingTireTargetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingTireTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRollingTireTargetAdjuster(var cap: Double) : RollingTireTargetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingTireTarget, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling tire target steps in order and keeps a trace of each. */
data class RollingTireTargetEngine(
    var steps: MutableList<RollingTireTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingTireTarget): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingTireTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingTireTargetEngine(flags: Registry): RollingTireTargetEngine {
    return RollingTireTargetEngine(
        steps = mutableListOf<RollingTireTargetStep>(
            RollingTireTargetFloorValueOffsetAdjuster(1.22),
            PercentRollingTireTargetAdjuster(3.9),
            FlaggedRollingTireTargetAdjuster(flags, "rolling-tire-target-scheduled", 7.4),
            RoundingRollingTireTargetAdjuster(0.1),
            TieredRollingTireTargetAdjuster(
                mutableListOf<Double>(2619.0, 3896.0, 8056.0),
                mutableListOf<Double>(0.88, 0.92, 1.03),
            ),
            CapRollingTireTargetAdjuster(33315.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 86946.9,
    )
}
