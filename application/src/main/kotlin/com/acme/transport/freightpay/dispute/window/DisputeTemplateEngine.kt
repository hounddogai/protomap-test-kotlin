package com.acme.transport.freightpay.dispute.window

import com.acme.transport.kit.flags.Registry

/** One step of the dispute template calculation. */
interface DisputeTemplateStep {
    fun describe(): String
    fun adjust(item: DisputeTemplate, value: Double): Double
}

data class DisputeTemplateDiscountOffsetAdjuster(var weight: Double) : DisputeTemplateStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: DisputeTemplate, value: Double): Double = value + item.discount * weight
}

data class ChannelDisputeTemplateAdjuster(var factors: MutableMap<String, Double>) : DisputeTemplateStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: DisputeTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDisputeTemplateAdjuster(var cap: Double) : DisputeTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DisputeTemplate, value: Double): Double = minOf(value, cap)
}

data class TieredDisputeTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputeTemplateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputeTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDisputeTemplateAdjuster(var minimum: Double) : DisputeTemplateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputeTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDisputeTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DisputeTemplateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DisputeTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the dispute template steps in order and keeps a trace of each. */
data class DisputeTemplateEngine(
    var steps: MutableList<DisputeTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DisputeTemplate): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DisputeTemplateStep = steps[index]
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

    fun evaluateAll(items: MutableList<DisputeTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDisputeTemplateEngine(flags: Registry): DisputeTemplateEngine {
    return DisputeTemplateEngine(
        steps = mutableListOf<DisputeTemplateStep>(
            DisputeTemplateDiscountOffsetAdjuster(1.6),
            ChannelDisputeTemplateAdjuster(mutableMapOf<String, Double>("west" to 1.09)),
            CapDisputeTemplateAdjuster(43880.0),
            TieredDisputeTemplateAdjuster(
                mutableListOf<Double>(203.0, 2485.0, 4783.0, 4924.0),
                mutableListOf<Double>(0.9, 1.2, 0.96, 1.16),
            ),
            MinimumDisputeTemplateAdjuster(14.0),
            FlaggedDisputeTemplateAdjuster(flags, "dispute-template-global", -1.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 40351.7,
    )
}
