package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lease return summary calculation. */
interface LeaseReturnSummaryFactor {
    fun describe(): String
    fun adjust(item: LeaseReturnSummary, value: Double): Double
}

data class MinimumLeaseReturnSummaryAdjuster(var minimum: Double) : LeaseReturnSummaryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLeaseReturnSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnSummaryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLeaseReturnSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnSummaryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLeaseReturnSummaryAdjuster(var step: Double) : LeaseReturnSummaryFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnSummary, value: Double): Double = roundTo(value, step)
}

data class CapLeaseReturnSummaryAdjuster(var cap: Double) : LeaseReturnSummaryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LeaseReturnSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the lease return summary steps in order and keeps a trace of each. */
data class LeaseReturnSummaryEngine(
    var steps: MutableList<LeaseReturnSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LeaseReturnSummary): Double {
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

    fun evaluateAll(items: MutableList<LeaseReturnSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLeaseReturnSummaryEngine(flags: Registry): LeaseReturnSummaryEngine {
    return LeaseReturnSummaryEngine(
        steps = mutableListOf<LeaseReturnSummaryFactor>(
            MinimumLeaseReturnSummaryAdjuster(38.0),
            FlaggedLeaseReturnSummaryAdjuster(flags, "lease-return-summary-regional", -7.3),
            TieredLeaseReturnSummaryAdjuster(
                mutableListOf<Double>(400.0, 4651.0, 4863.0, 9765.0),
                mutableListOf<Double>(0.93, 0.92, 1.1, 1.09),
            ),
            RoundingLeaseReturnSummaryAdjuster(0.5),
            CapLeaseReturnSummaryAdjuster(17452.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 43649.4,
    )
}
