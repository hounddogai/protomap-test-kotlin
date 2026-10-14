package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.flags.Registry

/** One step of the primary tender quota calculation. */
interface PrimaryTenderQuotaModifier {
    fun describe(): String
    fun adjust(item: PrimaryTenderQuota, value: Double): Double
}

data class TieredPrimaryTenderQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryTenderQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryTenderQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPrimaryTenderQuotaAdjuster(var minimum: Double) : PrimaryTenderQuotaModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryTenderQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPrimaryTenderQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryTenderQuotaModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryTenderQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PrimaryTenderQuotaUpdatedAtOffsetAdjuster(var weight: Double) : PrimaryTenderQuotaModifier {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: PrimaryTenderQuota, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

/** Runs the primary tender quota steps in order and keeps a trace of each. */
data class PrimaryTenderQuotaEngine(
    var steps: MutableList<PrimaryTenderQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryTenderQuota): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryTenderQuotaModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryTenderQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryTenderQuotaEngine(flags: Registry): PrimaryTenderQuotaEngine {
    return PrimaryTenderQuotaEngine(
        steps = mutableListOf<PrimaryTenderQuotaModifier>(
            TieredPrimaryTenderQuotaAdjuster(
                mutableListOf<Double>(2423.0, 8417.0),
                mutableListOf<Double>(0.94, 1.08),
            ),
            MinimumPrimaryTenderQuotaAdjuster(41.7),
            FlaggedPrimaryTenderQuotaAdjuster(flags, "primary-tender-quota-default", 7.3),
            PrimaryTenderQuotaUpdatedAtOffsetAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 38172.1,
    )
}
