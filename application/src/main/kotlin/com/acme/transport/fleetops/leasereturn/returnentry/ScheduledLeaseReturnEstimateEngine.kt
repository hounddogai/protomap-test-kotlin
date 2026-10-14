package com.acme.transport.fleetops.leasereturn.returnentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled lease return estimate calculation. */
interface ScheduledLeaseReturnEstimateStep {
    fun describe(): String
    fun adjust(item: ScheduledLeaseReturnEstimate, value: Double): Double
}

data class FlaggedScheduledLeaseReturnEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledLeaseReturnEstimateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledLeaseReturnEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScheduledLeaseReturnEstimateLoadKgOffsetAdjuster(
    var weight: Double,
) : ScheduledLeaseReturnEstimateStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: ScheduledLeaseReturnEstimate, value: Double): Double = value + item.loadKg * weight
}

data class MinimumScheduledLeaseReturnEstimateAdjuster(var minimum: Double) : ScheduledLeaseReturnEstimateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledLeaseReturnEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentScheduledLeaseReturnEstimateAdjuster(var percent: Double) : ScheduledLeaseReturnEstimateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledLeaseReturnEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneScheduledLeaseReturnEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledLeaseReturnEstimateStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: ScheduledLeaseReturnEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredScheduledLeaseReturnEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledLeaseReturnEstimateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledLeaseReturnEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled lease return estimate steps in order and keeps a trace of each. */
data class ScheduledLeaseReturnEstimateEngine(
    var steps: MutableList<ScheduledLeaseReturnEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledLeaseReturnEstimate): Double {
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

    fun evaluateAll(items: MutableList<ScheduledLeaseReturnEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledLeaseReturnEstimateEngine(flags: Registry): ScheduledLeaseReturnEstimateEngine {
    return ScheduledLeaseReturnEstimateEngine(
        steps = mutableListOf<ScheduledLeaseReturnEstimateStep>(
            FlaggedScheduledLeaseReturnEstimateAdjuster(flags, "scheduled-lease-return-estimate-fallback", 6.5),
            ScheduledLeaseReturnEstimateLoadKgOffsetAdjuster(0.66),
            MinimumScheduledLeaseReturnEstimateAdjuster(41.7),
            PercentScheduledLeaseReturnEstimateAdjuster(7.5),
            LaneScheduledLeaseReturnEstimateAdjuster(
                mutableMapOf<String, Double>("west" to 1.1, "central" to 0.82, "east" to 1.26),
            ),
            TieredScheduledLeaseReturnEstimateAdjuster(
                mutableListOf<Double>(374.0, 6332.0),
                mutableListOf<Double>(1.06, 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 23904.5,
    )
}
