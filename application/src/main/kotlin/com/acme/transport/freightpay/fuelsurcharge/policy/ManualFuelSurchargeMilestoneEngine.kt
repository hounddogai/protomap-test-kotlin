package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.kit.flags.Registry

/** One step of the manual fuel surcharge milestone calculation. */
interface ManualFuelSurchargeMilestoneModifier {
    fun describe(): String
    fun adjust(item: ManualFuelSurchargeMilestone, value: Double): Double
}

data class MinimumManualFuelSurchargeMilestoneAdjuster(
    var minimum: Double,
) : ManualFuelSurchargeMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualFuelSurchargeMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedManualFuelSurchargeMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualFuelSurchargeMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualFuelSurchargeMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentManualFuelSurchargeMilestoneAdjuster(
    var percent: Double,
) : ManualFuelSurchargeMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualFuelSurchargeMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the manual fuel surcharge milestone steps in order and keeps a trace of each. */
data class ManualFuelSurchargeMilestoneEngine(
    var steps: MutableList<ManualFuelSurchargeMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualFuelSurchargeMilestone): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualFuelSurchargeMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualFuelSurchargeMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualFuelSurchargeMilestoneEngine(flags: Registry): ManualFuelSurchargeMilestoneEngine {
    return ManualFuelSurchargeMilestoneEngine(
        steps = mutableListOf<ManualFuelSurchargeMilestoneModifier>(
            MinimumManualFuelSurchargeMilestoneAdjuster(20.8),
            FlaggedManualFuelSurchargeMilestoneAdjuster(flags, "manual-fuel-surcharge-milestone-adaptive", -3.7),
            PercentManualFuelSurchargeMilestoneAdjuster(0.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 16633.6,
    )
}
