package com.acme.transport.freightpay.dispute.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending dispute baseline calculation. */
interface PendingDisputeBaselineFactor {
    fun describe(): String
    fun adjust(item: PendingDisputeBaseline, value: Double): Double
}

data class TieredPendingDisputeBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingDisputeBaselineFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingDisputeBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPendingDisputeBaselineAdjuster(var percent: Double) : PendingDisputeBaselineFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingDisputeBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPendingDisputeBaselineAdjuster(var cap: Double) : PendingDisputeBaselineFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingDisputeBaseline, value: Double): Double = minOf(value, cap)
}

data class MinimumPendingDisputeBaselineAdjuster(var minimum: Double) : PendingDisputeBaselineFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingDisputeBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPendingDisputeBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingDisputeBaselineFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingDisputeBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPendingDisputeBaselineAdjuster(var step: Double) : PendingDisputeBaselineFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingDisputeBaseline, value: Double): Double = roundTo(value, step)
}

/** Runs the pending dispute baseline steps in order and keeps a trace of each. */
data class PendingDisputeBaselineEngine(
    var steps: MutableList<PendingDisputeBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingDisputeBaseline): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingDisputeBaselineFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingDisputeBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingDisputeBaselineEngine(flags: Registry): PendingDisputeBaselineEngine {
    return PendingDisputeBaselineEngine(
        steps = mutableListOf<PendingDisputeBaselineFactor>(
            TieredPendingDisputeBaselineAdjuster(
                mutableListOf<Double>(836.0, 5543.0, 8393.0),
                mutableListOf<Double>(0.91, 1.16, 1.04),
            ),
            PercentPendingDisputeBaselineAdjuster(-3.6),
            CapPendingDisputeBaselineAdjuster(18767.0),
            MinimumPendingDisputeBaselineAdjuster(9.7),
            FlaggedPendingDisputeBaselineAdjuster(flags, "pending-dispute-baseline-strict", 5.2),
            RoundingPendingDisputeBaselineAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 74771.8,
    )
}
