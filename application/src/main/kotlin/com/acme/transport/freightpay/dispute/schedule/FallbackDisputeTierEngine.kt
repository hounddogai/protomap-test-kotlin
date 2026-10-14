package com.acme.transport.freightpay.dispute.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback dispute tier calculation. */
interface FallbackDisputeTierFactor {
    fun describe(): String
    fun adjust(item: FallbackDisputeTier, value: Double): Double
}

data class FlaggedFallbackDisputeTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackDisputeTierFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackDisputeTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackDisputeTierAdjuster(var cap: Double) : FallbackDisputeTierFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDisputeTier, value: Double): Double = minOf(value, cap)
}

data class MinimumFallbackDisputeTierAdjuster(var minimum: Double) : FallbackDisputeTierFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackDisputeTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FallbackDisputeTierCreatedAtOffsetAdjuster(var weight: Double) : FallbackDisputeTierFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: FallbackDisputeTier, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class PercentFallbackDisputeTierAdjuster(var percent: Double) : FallbackDisputeTierFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackDisputeTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback dispute tier steps in order and keeps a trace of each. */
data class FallbackDisputeTierEngine(
    var steps: MutableList<FallbackDisputeTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackDisputeTier): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackDisputeTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackDisputeTierEngine(flags: Registry): FallbackDisputeTierEngine {
    return FallbackDisputeTierEngine(
        steps = mutableListOf<FallbackDisputeTierFactor>(
            FlaggedFallbackDisputeTierAdjuster(flags, "fallback-dispute-tier-weighted", 4.3),
            CapFallbackDisputeTierAdjuster(11390.0),
            MinimumFallbackDisputeTierAdjuster(44.4),
            FallbackDisputeTierCreatedAtOffsetAdjuster(0.36),
            PercentFallbackDisputeTierAdjuster(-2.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 1758.8,
    )
}
