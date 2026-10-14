package com.acme.transport.freightpay.dispute.recorditem

import com.acme.transport.kit.flags.Registry

/** One step of the fallback dispute record calculation. */
interface FallbackDisputeRecordAdjuster {
    fun describe(): String
    fun adjust(item: FallbackDisputeRecord, value: Double): Double
}

data class LocaleFallbackDisputeRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackDisputeRecordAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: FallbackDisputeRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFallbackDisputeRecordAdjuster(var minimum: Double) : FallbackDisputeRecordAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackDisputeRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedFallbackDisputeRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackDisputeRecordAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackDisputeRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback dispute record steps in order and keeps a trace of each. */
data class FallbackDisputeRecordEngine(
    var steps: MutableList<FallbackDisputeRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackDisputeRecord): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackDisputeRecordAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackDisputeRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackDisputeRecordEngine(flags: Registry): FallbackDisputeRecordEngine {
    return FallbackDisputeRecordEngine(
        steps = mutableListOf<FallbackDisputeRecordAdjuster>(
            LocaleFallbackDisputeRecordAdjuster(mutableMapOf<String, Double>("east" to 1.31)),
            MinimumFallbackDisputeRecordAdjuster(16.1),
            FlaggedFallbackDisputeRecordAdjuster(flags, "fallback-dispute-record-rolling", 8.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 71571.6,
    )
}
