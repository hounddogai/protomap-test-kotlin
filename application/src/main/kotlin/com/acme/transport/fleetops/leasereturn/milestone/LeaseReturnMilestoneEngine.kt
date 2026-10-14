package com.acme.transport.fleetops.leasereturn.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lease return milestone calculation. */
interface LeaseReturnMilestoneStep {
    fun describe(): String
    fun adjust(item: LeaseReturnMilestone, value: Double): Double
}

data class TieredLeaseReturnMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnMilestoneStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLeaseReturnMilestoneAdjuster(var percent: Double) : LeaseReturnMilestoneStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LeaseReturnMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLeaseReturnMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnMilestoneStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lease return milestone steps in order and keeps a trace of each. */
data class LeaseReturnMilestoneEngine(
    var steps: MutableList<LeaseReturnMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LeaseReturnMilestone): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LeaseReturnMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLeaseReturnMilestoneEngine(flags: Registry): LeaseReturnMilestoneEngine {
    return LeaseReturnMilestoneEngine(
        steps = mutableListOf<LeaseReturnMilestoneStep>(
            TieredLeaseReturnMilestoneAdjuster(
                mutableListOf<Double>(2049.0, 2408.0, 3676.0, 6957.0),
                mutableListOf<Double>(1.02, 1.01, 1.11, 0.9),
            ),
            PercentLeaseReturnMilestoneAdjuster(24.0),
            FlaggedLeaseReturnMilestoneAdjuster(flags, "lease-return-milestone-tiered", -0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 56546.1,
    )
}
