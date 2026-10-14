package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the accessorial template calculation. */
interface AccessorialTemplateAdjuster {
    fun describe(): String
    fun adjust(item: AccessorialTemplate, value: Double): Double
}

data class FlaggedAccessorialTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AccessorialTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AccessorialTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAccessorialTemplateAdjuster(var minimum: Double) : AccessorialTemplateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ChannelAccessorialTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : AccessorialTemplateAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: AccessorialTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAccessorialTemplateAdjuster(var step: Double) : AccessorialTemplateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialTemplate, value: Double): Double = roundTo(value, step)
}

data class TieredAccessorialTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AccessorialTemplateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AccessorialTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the accessorial template steps in order and keeps a trace of each. */
data class AccessorialTemplateEngine(
    var steps: MutableList<AccessorialTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AccessorialTemplate): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AccessorialTemplateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AccessorialTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAccessorialTemplateEngine(flags: Registry): AccessorialTemplateEngine {
    return AccessorialTemplateEngine(
        steps = mutableListOf<AccessorialTemplateAdjuster>(
            FlaggedAccessorialTemplateAdjuster(flags, "accessorial-template-strict", 4.1),
            MinimumAccessorialTemplateAdjuster(39.4),
            ChannelAccessorialTemplateAdjuster(mutableMapOf<String, Double>("online" to 1.34, "south" to 0.8)),
            RoundingAccessorialTemplateAdjuster(0.5),
            TieredAccessorialTemplateAdjuster(
                mutableListOf<Double>(5515.0, 6263.0, 9820.0),
                mutableListOf<Double>(0.89, 0.96, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 4134.4,
    )
}
