package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.flags.Registry

/** One step of the broker template calculation. */
interface BrokerTemplateFactor {
    fun describe(): String
    fun adjust(item: BrokerTemplate, value: Double): Double
}

data class CapBrokerTemplateAdjuster(var cap: Double) : BrokerTemplateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BrokerTemplate, value: Double): Double = minOf(value, cap)
}

data class BrokerTemplateAttemptsOffsetAdjuster(var weight: Double) : BrokerTemplateFactor {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: BrokerTemplate, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class MinimumBrokerTemplateAdjuster(var minimum: Double) : BrokerTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BrokerTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBrokerTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BrokerTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BrokerTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBrokerTemplateAdjuster(var percent: Double) : BrokerTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the broker template steps in order and keeps a trace of each. */
data class BrokerTemplateEngine(
    var steps: MutableList<BrokerTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BrokerTemplate): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BrokerTemplateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BrokerTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBrokerTemplateEngine(flags: Registry): BrokerTemplateEngine {
    return BrokerTemplateEngine(
        steps = mutableListOf<BrokerTemplateFactor>(
            CapBrokerTemplateAdjuster(29157.0),
            BrokerTemplateAttemptsOffsetAdjuster(0.63),
            MinimumBrokerTemplateAdjuster(37.1),
            FlaggedBrokerTemplateAdjuster(flags, "broker-template-legacy", 9.6),
            PercentBrokerTemplateAdjuster(-2.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 26290.5,
    )
}
