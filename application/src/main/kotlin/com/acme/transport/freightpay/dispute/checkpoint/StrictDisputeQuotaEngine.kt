package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.flags.Registry

/** One step of the strict dispute quota calculation. */
interface StrictDisputeQuotaFactor {
    fun describe(): String
    fun adjust(item: StrictDisputeQuota, value: Double): Double
}

data class PercentStrictDisputeQuotaAdjuster(var percent: Double) : StrictDisputeQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictDisputeQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleStrictDisputeQuotaAdjuster(var factors: MutableMap<String, Double>) : StrictDisputeQuotaFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictDisputeQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class StrictDisputeQuotaThresholdOffsetAdjuster(var weight: Double) : StrictDisputeQuotaFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: StrictDisputeQuota, value: Double): Double = value + item.threshold * weight
}

data class FlaggedStrictDisputeQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictDisputeQuotaFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictDisputeQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the strict dispute quota steps in order and keeps a trace of each. */
data class StrictDisputeQuotaEngine(
    var steps: MutableList<StrictDisputeQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictDisputeQuota): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictDisputeQuotaFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictDisputeQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictDisputeQuotaEngine(flags: Registry): StrictDisputeQuotaEngine {
    return StrictDisputeQuotaEngine(
        steps = mutableListOf<StrictDisputeQuotaFactor>(
            PercentStrictDisputeQuotaAdjuster(8.8),
            TitleStrictDisputeQuotaAdjuster(
                mutableMapOf<String, Double>("central" to 1.3, "west" to 0.88, "retail" to 1.06),
            ),
            StrictDisputeQuotaThresholdOffsetAdjuster(0.04),
            FlaggedStrictDisputeQuotaAdjuster(flags, "strict-dispute-quota-weighted", -4.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 71667.1,
    )
}
