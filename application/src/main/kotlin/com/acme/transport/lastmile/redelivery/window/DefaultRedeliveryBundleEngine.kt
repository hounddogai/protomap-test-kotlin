package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default redelivery bundle calculation. */
interface DefaultRedeliveryBundleFactor {
    fun describe(): String
    fun adjust(item: DefaultRedeliveryBundle, value: Double): Double
}

data class RoundingDefaultRedeliveryBundleAdjuster(var step: Double) : DefaultRedeliveryBundleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultRedeliveryBundle, value: Double): Double = roundTo(value, step)
}

data class DefaultRedeliveryBundleGraceDaysOffsetAdjuster(var weight: Double) : DefaultRedeliveryBundleFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: DefaultRedeliveryBundle, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class FlaggedDefaultRedeliveryBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultRedeliveryBundleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultRedeliveryBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDefaultRedeliveryBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultRedeliveryBundleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultRedeliveryBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDefaultRedeliveryBundleAdjuster(var percent: Double) : DefaultRedeliveryBundleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultRedeliveryBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDefaultRedeliveryBundleAdjuster(var cap: Double) : DefaultRedeliveryBundleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultRedeliveryBundle, value: Double): Double = minOf(value, cap)
}

/** Runs the default redelivery bundle steps in order and keeps a trace of each. */
data class DefaultRedeliveryBundleEngine(
    var steps: MutableList<DefaultRedeliveryBundleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultRedeliveryBundle): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultRedeliveryBundleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultRedeliveryBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultRedeliveryBundleEngine(flags: Registry): DefaultRedeliveryBundleEngine {
    return DefaultRedeliveryBundleEngine(
        steps = mutableListOf<DefaultRedeliveryBundleFactor>(
            RoundingDefaultRedeliveryBundleAdjuster(0.25),
            DefaultRedeliveryBundleGraceDaysOffsetAdjuster(0.7),
            FlaggedDefaultRedeliveryBundleAdjuster(flags, "default-redelivery-bundle-rolling", -6.3),
            TieredDefaultRedeliveryBundleAdjuster(
                mutableListOf<Double>(2371.0, 9525.0),
                mutableListOf<Double>(1.11, 0.9),
            ),
            PercentDefaultRedeliveryBundleAdjuster(10.3),
            CapDefaultRedeliveryBundleAdjuster(41346.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 15303.8,
    )
}
