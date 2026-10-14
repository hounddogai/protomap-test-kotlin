package com.acme.transport.linehaul.tender.slot

import com.acme.transport.kit.flags.Registry

/** One step of the weighted tender quota calculation. */
interface WeightedTenderQuotaAdjuster {
    fun describe(): String
    fun adjust(item: WeightedTenderQuota, value: Double): Double
}

data class FlaggedWeightedTenderQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedTenderQuotaAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedTenderQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumWeightedTenderQuotaAdjuster(var minimum: Double) : WeightedTenderQuotaAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedTenderQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class WeightedTenderQuotaWeightOffsetAdjuster(var weight: Double) : WeightedTenderQuotaAdjuster {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: WeightedTenderQuota, value: Double): Double = value + item.weight * weight
}

/** Runs the weighted tender quota steps in order and keeps a trace of each. */
data class WeightedTenderQuotaEngine(
    var steps: MutableList<WeightedTenderQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedTenderQuota): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedTenderQuotaAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedTenderQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedTenderQuotaEngine(flags: Registry): WeightedTenderQuotaEngine {
    return WeightedTenderQuotaEngine(
        steps = mutableListOf<WeightedTenderQuotaAdjuster>(
            FlaggedWeightedTenderQuotaAdjuster(flags, "weighted-tender-quota-pending", 8.2),
            MinimumWeightedTenderQuotaAdjuster(2.3),
            WeightedTenderQuotaWeightOffsetAdjuster(0.06),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 92916.3,
    )
}
