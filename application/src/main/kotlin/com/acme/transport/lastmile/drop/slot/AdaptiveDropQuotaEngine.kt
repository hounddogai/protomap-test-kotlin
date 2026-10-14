package com.acme.transport.lastmile.drop.slot

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive drop quota calculation. */
interface AdaptiveDropQuotaModifier {
    fun describe(): String
    fun adjust(item: AdaptiveDropQuota, value: Double): Double
}

data class TieredAdaptiveDropQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveDropQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveDropQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleAdaptiveDropQuotaAdjuster(var factors: MutableMap<String, Double>) : AdaptiveDropQuotaModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveDropQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAdaptiveDropQuotaAdjuster(var cap: Double) : AdaptiveDropQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveDropQuota, value: Double): Double = minOf(value, cap)
}

data class FlaggedAdaptiveDropQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveDropQuotaModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveDropQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive drop quota steps in order and keeps a trace of each. */
data class AdaptiveDropQuotaEngine(
    var steps: MutableList<AdaptiveDropQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveDropQuota): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveDropQuotaModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveDropQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveDropQuotaEngine(flags: Registry): AdaptiveDropQuotaEngine {
    return AdaptiveDropQuotaEngine(
        steps = mutableListOf<AdaptiveDropQuotaModifier>(
            TieredAdaptiveDropQuotaAdjuster(
                mutableListOf<Double>(6218.0, 6339.0),
                mutableListOf<Double>(0.82, 0.96),
            ),
            TitleAdaptiveDropQuotaAdjuster(mutableMapOf<String, Double>("central" to 0.87)),
            CapAdaptiveDropQuotaAdjuster(35732.0),
            FlaggedAdaptiveDropQuotaAdjuster(flags, "adaptive-drop-quota-tiered", 3.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 58680.4,
    )
}
