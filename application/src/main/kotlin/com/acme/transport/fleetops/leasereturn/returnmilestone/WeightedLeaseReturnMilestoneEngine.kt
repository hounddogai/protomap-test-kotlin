package com.acme.transport.fleetops.leasereturn.returnmilestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted lease return milestone calculation. */
interface WeightedLeaseReturnMilestoneStep {
    fun describe(): String
    fun adjust(item: WeightedLeaseReturnMilestone, value: Double): Double
}

data class MinimumWeightedLeaseReturnMilestoneAdjuster(var minimum: Double) : WeightedLeaseReturnMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedLeaseReturnMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentWeightedLeaseReturnMilestoneAdjuster(var percent: Double) : WeightedLeaseReturnMilestoneStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedLeaseReturnMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedWeightedLeaseReturnMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedLeaseReturnMilestoneStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedLeaseReturnMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class WeightedLeaseReturnMilestoneVersionOffsetAdjuster(
    var weight: Double,
) : WeightedLeaseReturnMilestoneStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: WeightedLeaseReturnMilestone, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

/** Runs the weighted lease return milestone steps in order and keeps a trace of each. */
data class WeightedLeaseReturnMilestoneEngine(
    var steps: MutableList<WeightedLeaseReturnMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedLeaseReturnMilestone): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedLeaseReturnMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedLeaseReturnMilestoneEngine(flags: Registry): WeightedLeaseReturnMilestoneEngine {
    return WeightedLeaseReturnMilestoneEngine(
        steps = mutableListOf<WeightedLeaseReturnMilestoneStep>(
            MinimumWeightedLeaseReturnMilestoneAdjuster(10.1),
            PercentWeightedLeaseReturnMilestoneAdjuster(-19.8),
            FlaggedWeightedLeaseReturnMilestoneAdjuster(flags, "weighted-lease-return-milestone-primary", -1.5),
            WeightedLeaseReturnMilestoneVersionOffsetAdjuster(0.04),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 20620.6,
    )
}
