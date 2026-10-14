package com.acme.transport.freightpay.ratecard.draft

import com.acme.transport.kit.flags.Registry

/** One step of the rate card template calculation. */
interface RateCardTemplateAdjuster {
    fun describe(): String
    fun adjust(item: RateCardTemplate, value: Double): Double
}

data class FlaggedRateCardTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RateCardTemplateVersionOffsetAdjuster(var weight: Double) : RateCardTemplateAdjuster {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: RateCardTemplate, value: Double): Double = value + item.version.toDouble() * weight
}

data class PercentRateCardTemplateAdjuster(var percent: Double) : RateCardTemplateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rate card template steps in order and keeps a trace of each. */
data class RateCardTemplateEngine(
    var steps: MutableList<RateCardTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RateCardTemplate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RateCardTemplateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RateCardTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRateCardTemplateEngine(flags: Registry): RateCardTemplateEngine {
    return RateCardTemplateEngine(
        steps = mutableListOf<RateCardTemplateAdjuster>(
            FlaggedRateCardTemplateAdjuster(flags, "rate-card-template-scheduled", -9.0),
            RateCardTemplateVersionOffsetAdjuster(1.14),
            PercentRateCardTemplateAdjuster(-13.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 60504.0,
    )
}
