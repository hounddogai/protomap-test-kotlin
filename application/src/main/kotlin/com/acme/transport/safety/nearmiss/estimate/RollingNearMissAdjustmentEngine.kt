package com.acme.transport.safety.nearmiss.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling near miss adjustment calculation. */
interface RollingNearMissAdjustmentStep {
    fun describe(): String
    fun adjust(item: RollingNearMissAdjustment, value: Double): Double
}

data class RoundingRollingNearMissAdjustmentAdjuster(var step: Double) : RollingNearMissAdjustmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingNearMissAdjustment, value: Double): Double = roundTo(value, step)
}

data class TieredRollingNearMissAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingNearMissAdjustmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingNearMissAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRollingNearMissAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingNearMissAdjustmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingNearMissAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRollingNearMissAdjustmentAdjuster(var percent: Double) : RollingNearMissAdjustmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingNearMissAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rolling near miss adjustment steps in order and keeps a trace of each. */
data class RollingNearMissAdjustmentEngine(
    var steps: MutableList<RollingNearMissAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingNearMissAdjustment): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingNearMissAdjustmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<RollingNearMissAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingNearMissAdjustmentEngine(flags: Registry): RollingNearMissAdjustmentEngine {
    return RollingNearMissAdjustmentEngine(
        steps = mutableListOf<RollingNearMissAdjustmentStep>(
            RoundingRollingNearMissAdjustmentAdjuster(0.05),
            TieredRollingNearMissAdjustmentAdjuster(
                mutableListOf<Double>(1017.0, 8573.0),
                mutableListOf<Double>(1.0, 1.07),
            ),
            FlaggedRollingNearMissAdjustmentAdjuster(flags, "rolling-near-miss-adjustment-weighted", -2.5),
            PercentRollingNearMissAdjustmentAdjuster(-10.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 12277.8,
    )
}
