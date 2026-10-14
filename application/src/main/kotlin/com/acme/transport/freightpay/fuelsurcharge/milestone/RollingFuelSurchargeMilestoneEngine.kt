package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling fuel surcharge milestone calculation. */
interface RollingFuelSurchargeMilestoneFactor {
    fun describe(): String
    fun adjust(item: RollingFuelSurchargeMilestone, value: Double): Double
}

data class RoundingRollingFuelSurchargeMilestoneAdjuster(
    var step: Double,
) : RollingFuelSurchargeMilestoneFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingFuelSurchargeMilestone, value: Double): Double = roundTo(value, step)
}

data class FlaggedRollingFuelSurchargeMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingFuelSurchargeMilestoneFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingFuelSurchargeMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRollingFuelSurchargeMilestoneAdjuster(var cap: Double) : RollingFuelSurchargeMilestoneFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingFuelSurchargeMilestone, value: Double): Double = minOf(value, cap)
}

data class TieredRollingFuelSurchargeMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingFuelSurchargeMilestoneFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingFuelSurchargeMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRollingFuelSurchargeMilestoneAdjuster(
    var percent: Double,
) : RollingFuelSurchargeMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingFuelSurchargeMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rolling fuel surcharge milestone steps in order and keeps a trace of each. */
data class RollingFuelSurchargeMilestoneEngine(
    var steps: MutableList<RollingFuelSurchargeMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingFuelSurchargeMilestone): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingFuelSurchargeMilestoneFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingFuelSurchargeMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingFuelSurchargeMilestoneEngine(flags: Registry): RollingFuelSurchargeMilestoneEngine {
    return RollingFuelSurchargeMilestoneEngine(
        steps = mutableListOf<RollingFuelSurchargeMilestoneFactor>(
            RoundingRollingFuelSurchargeMilestoneAdjuster(0.1),
            FlaggedRollingFuelSurchargeMilestoneAdjuster(
                flags,
                "rolling-fuel-surcharge-milestone-regional",
                3.4,
            ),
            CapRollingFuelSurchargeMilestoneAdjuster(37345.0),
            TieredRollingFuelSurchargeMilestoneAdjuster(
                mutableListOf<Double>(1274.0, 4989.0, 6750.0, 7774.0),
                mutableListOf<Double>(1.15, 1.19, 0.84, 1.04),
            ),
            PercentRollingFuelSurchargeMilestoneAdjuster(23.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 60038.0,
    )
}
