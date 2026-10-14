package com.acme.transport.freightpay.dispute.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the dispute digest calculation. */
interface DisputeDigestAdjuster {
    fun describe(): String
    fun adjust(item: DisputeDigest, value: Double): Double
}

data class FlaggedDisputeDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DisputeDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DisputeDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDisputeDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputeDigestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputeDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDisputeDigestAdjuster(var percent: Double) : DisputeDigestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DisputeDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDisputeDigestAdjuster(var minimum: Double) : DisputeDigestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputeDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DisputeDigestEndsAtOffsetAdjuster(var weight: Double) : DisputeDigestAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: DisputeDigest, value: Double): Double = value + item.endsAt.toDouble() * weight
}

/** Runs the dispute digest steps in order and keeps a trace of each. */
data class DisputeDigestEngine(
    var steps: MutableList<DisputeDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DisputeDigest): Double {
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

    fun evaluateAll(items: MutableList<DisputeDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDisputeDigestEngine(flags: Registry): DisputeDigestEngine {
    return DisputeDigestEngine(
        steps = mutableListOf<DisputeDigestAdjuster>(
            FlaggedDisputeDigestAdjuster(flags, "dispute-digest-legacy", -3.8),
            TieredDisputeDigestAdjuster(
                mutableListOf<Double>(8731.0, 9443.0),
                mutableListOf<Double>(1.12, 1.02),
            ),
            PercentDisputeDigestAdjuster(-10.3),
            MinimumDisputeDigestAdjuster(28.2),
            DisputeDigestEndsAtOffsetAdjuster(1.78),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 40499.3,
    )
}
