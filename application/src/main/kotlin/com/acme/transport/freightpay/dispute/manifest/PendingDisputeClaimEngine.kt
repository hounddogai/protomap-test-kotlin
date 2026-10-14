package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.flags.Registry

/** One step of the pending dispute claim calculation. */
interface PendingDisputeClaimAdjuster {
    fun describe(): String
    fun adjust(item: PendingDisputeClaim, value: Double): Double
}

data class FlaggedPendingDisputeClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingDisputeClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingDisputeClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingDisputeClaimAdjuster(var minimum: Double) : PendingDisputeClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingDisputeClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPendingDisputeClaimAdjuster(var cap: Double) : PendingDisputeClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingDisputeClaim, value: Double): Double = minOf(value, cap)
}

data class TieredPendingDisputeClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingDisputeClaimAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingDisputeClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PendingDisputeClaimStopCountOffsetAdjuster(var weight: Double) : PendingDisputeClaimAdjuster {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: PendingDisputeClaim, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class PercentPendingDisputeClaimAdjuster(var percent: Double) : PendingDisputeClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingDisputeClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending dispute claim steps in order and keeps a trace of each. */
data class PendingDisputeClaimEngine(
    var steps: MutableList<PendingDisputeClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingDisputeClaim): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingDisputeClaimAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingDisputeClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingDisputeClaimEngine(flags: Registry): PendingDisputeClaimEngine {
    return PendingDisputeClaimEngine(
        steps = mutableListOf<PendingDisputeClaimAdjuster>(
            FlaggedPendingDisputeClaimAdjuster(flags, "pending-dispute-claim-scheduled", -0.6),
            MinimumPendingDisputeClaimAdjuster(47.6),
            CapPendingDisputeClaimAdjuster(2323.0),
            TieredPendingDisputeClaimAdjuster(
                mutableListOf<Double>(6476.0, 6551.0, 6772.0),
                mutableListOf<Double>(0.86, 1.12, 0.98),
            ),
            PendingDisputeClaimStopCountOffsetAdjuster(0.19),
            PercentPendingDisputeClaimAdjuster(-18.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 55924.0,
    )
}
