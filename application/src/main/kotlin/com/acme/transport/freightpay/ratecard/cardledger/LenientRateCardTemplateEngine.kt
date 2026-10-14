package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.flags.Registry

/** One step of the lenient rate card template calculation. */
interface LenientRateCardTemplateFactor {
    fun describe(): String
    fun adjust(item: LenientRateCardTemplate, value: Double): Double
}

data class MinimumLenientRateCardTemplateAdjuster(var minimum: Double) : LenientRateCardTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientRateCardTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLenientRateCardTemplateAdjuster(var percent: Double) : LenientRateCardTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientRateCardTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLenientRateCardTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientRateCardTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientRateCardTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLenientRateCardTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientRateCardTemplateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientRateCardTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourceLenientRateCardTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientRateCardTemplateFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: LenientRateCardTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient rate card template steps in order and keeps a trace of each. */
data class LenientRateCardTemplateEngine(
    var steps: MutableList<LenientRateCardTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientRateCardTemplate): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientRateCardTemplateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientRateCardTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientRateCardTemplateEngine(flags: Registry): LenientRateCardTemplateEngine {
    return LenientRateCardTemplateEngine(
        steps = mutableListOf<LenientRateCardTemplateFactor>(
            MinimumLenientRateCardTemplateAdjuster(3.8),
            PercentLenientRateCardTemplateAdjuster(-3.2),
            FlaggedLenientRateCardTemplateAdjuster(flags, "lenient-rate-card-template-capped", 9.7),
            TieredLenientRateCardTemplateAdjuster(
                mutableListOf<Double>(314.0, 1346.0, 6887.0, 8690.0),
                mutableListOf<Double>(0.88, 0.81, 0.82, 1.15),
            ),
            SourceLenientRateCardTemplateAdjuster(
                mutableMapOf<String, Double>("east" to 0.79, "central" to 0.76),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 38358.7,
    )
}
