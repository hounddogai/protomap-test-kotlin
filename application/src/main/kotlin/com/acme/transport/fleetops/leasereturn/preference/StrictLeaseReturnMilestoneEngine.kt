package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.flags.Registry

/** One step of the strict lease return milestone calculation. */
interface StrictLeaseReturnMilestoneModifier {
    fun describe(): String
    fun adjust(item: StrictLeaseReturnMilestone, value: Double): Double
}

data class PercentStrictLeaseReturnMilestoneAdjuster(var percent: Double) : StrictLeaseReturnMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictLeaseReturnMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedStrictLeaseReturnMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictLeaseReturnMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictLeaseReturnMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredStrictLeaseReturnMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictLeaseReturnMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictLeaseReturnMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class StrictLeaseReturnMilestoneMaximumOffsetAdjuster(
    var weight: Double,
) : StrictLeaseReturnMilestoneModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: StrictLeaseReturnMilestone, value: Double): Double = value + item.maximum * weight
}

/** Runs the strict lease return milestone steps in order and keeps a trace of each. */
data class StrictLeaseReturnMilestoneEngine(
    var steps: MutableList<StrictLeaseReturnMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictLeaseReturnMilestone): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictLeaseReturnMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictLeaseReturnMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictLeaseReturnMilestoneEngine(flags: Registry): StrictLeaseReturnMilestoneEngine {
    return StrictLeaseReturnMilestoneEngine(
        steps = mutableListOf<StrictLeaseReturnMilestoneModifier>(
            PercentStrictLeaseReturnMilestoneAdjuster(2.2),
            FlaggedStrictLeaseReturnMilestoneAdjuster(flags, "strict-lease-return-milestone-default", 5.5),
            TieredStrictLeaseReturnMilestoneAdjuster(
                mutableListOf<Double>(5017.0, 6591.0),
                mutableListOf<Double>(0.84, 1.16),
            ),
            StrictLeaseReturnMilestoneMaximumOffsetAdjuster(1.35),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 51770.9,
    )
}
