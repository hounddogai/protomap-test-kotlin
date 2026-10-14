package com.acme.transport.freightpay.dispute.disputeforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending dispute digest calculation. */
interface PendingDisputeDigestModifier {
    fun describe(): String
    fun adjust(item: PendingDisputeDigest, value: Double): Double
}

data class FlaggedPendingDisputeDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingDisputeDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingDisputeDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingDisputeDigestAdjuster(var minimum: Double) : PendingDisputeDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingDisputeDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotPendingDisputeDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingDisputeDigestModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: PendingDisputeDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPendingDisputeDigestAdjuster(var percent: Double) : PendingDisputeDigestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingDisputeDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending dispute digest steps in order and keeps a trace of each. */
data class PendingDisputeDigestEngine(
    var steps: MutableList<PendingDisputeDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingDisputeDigest): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingDisputeDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingDisputeDigestEngine(flags: Registry): PendingDisputeDigestEngine {
    return PendingDisputeDigestEngine(
        steps = mutableListOf<PendingDisputeDigestModifier>(
            FlaggedPendingDisputeDigestAdjuster(flags, "pending-dispute-digest-weighted", 2.0),
            MinimumPendingDisputeDigestAdjuster(10.1),
            DepotPendingDisputeDigestAdjuster(mutableMapOf<String, Double>("north" to 1.29, "online" to 1.36)),
            PercentPendingDisputeDigestAdjuster(19.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 41295.2,
    )
}
