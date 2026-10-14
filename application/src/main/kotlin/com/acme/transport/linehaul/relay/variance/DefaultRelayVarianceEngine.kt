package com.acme.transport.linehaul.relay.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default relay variance calculation. */
interface DefaultRelayVarianceModifier {
    fun describe(): String
    fun adjust(item: DefaultRelayVariance, value: Double): Double
}

data class FlaggedDefaultRelayVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultRelayVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultRelayVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDefaultRelayVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultRelayVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultRelayVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ChannelDefaultRelayVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultRelayVarianceModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: DefaultRelayVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDefaultRelayVarianceAdjuster(var minimum: Double) : DefaultRelayVarianceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultRelayVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDefaultRelayVarianceAdjuster(var step: Double) : DefaultRelayVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultRelayVariance, value: Double): Double = roundTo(value, step)
}

data class CapDefaultRelayVarianceAdjuster(var cap: Double) : DefaultRelayVarianceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultRelayVariance, value: Double): Double = minOf(value, cap)
}

/** Runs the default relay variance steps in order and keeps a trace of each. */
data class DefaultRelayVarianceEngine(
    var steps: MutableList<DefaultRelayVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultRelayVariance): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultRelayVarianceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultRelayVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultRelayVarianceEngine(flags: Registry): DefaultRelayVarianceEngine {
    return DefaultRelayVarianceEngine(
        steps = mutableListOf<DefaultRelayVarianceModifier>(
            FlaggedDefaultRelayVarianceAdjuster(flags, "default-relay-variance-lenient", 5.6),
            TieredDefaultRelayVarianceAdjuster(
                mutableListOf<Double>(505.0, 5511.0, 6869.0),
                mutableListOf<Double>(0.92, 0.89, 1.1),
            ),
            ChannelDefaultRelayVarianceAdjuster(mutableMapOf<String, Double>("retail" to 1.37)),
            MinimumDefaultRelayVarianceAdjuster(37.6),
            RoundingDefaultRelayVarianceAdjuster(5.0),
            CapDefaultRelayVarianceAdjuster(44689.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 25157.7,
    )
}
