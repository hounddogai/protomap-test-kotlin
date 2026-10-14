package com.acme.transport.freightpay.accessorial.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default accessorial rollup calculation. */
interface DefaultAccessorialRollupModifier {
    fun describe(): String
    fun adjust(item: DefaultAccessorialRollup, value: Double): Double
}

data class ChannelDefaultAccessorialRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultAccessorialRollupModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: DefaultAccessorialRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDefaultAccessorialRollupAdjuster(var cap: Double) : DefaultAccessorialRollupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultAccessorialRollup, value: Double): Double = minOf(value, cap)
}

data class MinimumDefaultAccessorialRollupAdjuster(var minimum: Double) : DefaultAccessorialRollupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultAccessorialRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDefaultAccessorialRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultAccessorialRollupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultAccessorialRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDefaultAccessorialRollupAdjuster(var step: Double) : DefaultAccessorialRollupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultAccessorialRollup, value: Double): Double = roundTo(value, step)
}

data class FlaggedDefaultAccessorialRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultAccessorialRollupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultAccessorialRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default accessorial rollup steps in order and keeps a trace of each. */
data class DefaultAccessorialRollupEngine(
    var steps: MutableList<DefaultAccessorialRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultAccessorialRollup): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultAccessorialRollupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultAccessorialRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultAccessorialRollupEngine(flags: Registry): DefaultAccessorialRollupEngine {
    return DefaultAccessorialRollupEngine(
        steps = mutableListOf<DefaultAccessorialRollupModifier>(
            ChannelDefaultAccessorialRollupAdjuster(
                mutableMapOf<String, Double>("central" to 1.18, "online" to 0.86, "west" to 0.77),
            ),
            CapDefaultAccessorialRollupAdjuster(29677.0),
            MinimumDefaultAccessorialRollupAdjuster(9.1),
            TieredDefaultAccessorialRollupAdjuster(
                mutableListOf<Double>(6434.0, 8229.0, 9364.0),
                mutableListOf<Double>(0.9, 1.09, 0.96),
            ),
            RoundingDefaultAccessorialRollupAdjuster(1.0),
            FlaggedDefaultAccessorialRollupAdjuster(flags, "default-accessorial-rollup-default", 0.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 80923.2,
    )
}
