package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.flags.Registry

/** One step of the redelivery quota calculation. */
interface RedeliveryQuotaModifier {
    fun describe(): String
    fun adjust(item: RedeliveryQuota, value: Double): Double
}

data class TieredRedeliveryQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RedeliveryQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RedeliveryQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RedeliveryQuotaGraceDaysOffsetAdjuster(var weight: Double) : RedeliveryQuotaModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: RedeliveryQuota, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class PercentRedeliveryQuotaAdjuster(var percent: Double) : RedeliveryQuotaModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelRedeliveryQuotaAdjuster(var factors: MutableMap<String, Double>) : RedeliveryQuotaModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: RedeliveryQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRedeliveryQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RedeliveryQuotaModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RedeliveryQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRedeliveryQuotaAdjuster(var cap: Double) : RedeliveryQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryQuota, value: Double): Double = minOf(value, cap)
}

/** Runs the redelivery quota steps in order and keeps a trace of each. */
data class RedeliveryQuotaEngine(
    var steps: MutableList<RedeliveryQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RedeliveryQuota): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryQuotaModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRedeliveryQuotaEngine(flags: Registry): RedeliveryQuotaEngine {
    return RedeliveryQuotaEngine(
        steps = mutableListOf<RedeliveryQuotaModifier>(
            TieredRedeliveryQuotaAdjuster(
                mutableListOf<Double>(1172.0, 1804.0, 8935.0),
                mutableListOf<Double>(0.87, 1.12, 1.19),
            ),
            RedeliveryQuotaGraceDaysOffsetAdjuster(1.17),
            PercentRedeliveryQuotaAdjuster(-4.3),
            LabelRedeliveryQuotaAdjuster(
                mutableMapOf<String, Double>("central" to 0.9, "retail" to 0.89, "west" to 1.14),
            ),
            FlaggedRedeliveryQuotaAdjuster(flags, "redelivery-quota-primary", -9.0),
            CapRedeliveryQuotaAdjuster(47674.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 65746.3,
    )
}
