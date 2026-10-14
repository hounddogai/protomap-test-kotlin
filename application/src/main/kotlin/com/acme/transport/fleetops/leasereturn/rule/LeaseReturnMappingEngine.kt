package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lease return mapping calculation. */
interface LeaseReturnMappingAdjuster {
    fun describe(): String
    fun adjust(item: LeaseReturnMapping, value: Double): Double
}

data class FlaggedLeaseReturnMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnMappingAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LeaseReturnMappingExpiresAtOffsetAdjuster(var weight: Double) : LeaseReturnMappingAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: LeaseReturnMapping, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class MinimumLeaseReturnMappingAdjuster(var minimum: Double) : LeaseReturnMappingAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLeaseReturnMappingAdjuster(var cap: Double) : LeaseReturnMappingAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LeaseReturnMapping, value: Double): Double = minOf(value, cap)
}

data class TieredLeaseReturnMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lease return mapping steps in order and keeps a trace of each. */
data class LeaseReturnMappingEngine(
    var steps: MutableList<LeaseReturnMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LeaseReturnMapping): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LeaseReturnMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLeaseReturnMappingEngine(flags: Registry): LeaseReturnMappingEngine {
    return LeaseReturnMappingEngine(
        steps = mutableListOf<LeaseReturnMappingAdjuster>(
            FlaggedLeaseReturnMappingAdjuster(flags, "lease-return-mapping-pending", -3.0),
            LeaseReturnMappingExpiresAtOffsetAdjuster(1.21),
            MinimumLeaseReturnMappingAdjuster(46.8),
            CapLeaseReturnMappingAdjuster(48209.0),
            TieredLeaseReturnMappingAdjuster(
                mutableListOf<Double>(1440.0, 2157.0, 2704.0),
                mutableListOf<Double>(0.99, 1.07, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 48972.2,
    )
}
