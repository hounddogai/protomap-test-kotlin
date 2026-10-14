package com.acme.transport.customs.broker.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the broker metric calculation. */
interface BrokerMetricModifier {
    fun describe(): String
    fun adjust(item: BrokerMetric, value: Double): Double
}

data class CapBrokerMetricAdjuster(var cap: Double) : BrokerMetricModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BrokerMetric, value: Double): Double = minOf(value, cap)
}

data class FlaggedBrokerMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BrokerMetricModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BrokerMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SegmentBrokerMetricAdjuster(var factors: MutableMap<String, Double>) : BrokerMetricModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: BrokerMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingBrokerMetricAdjuster(var step: Double) : BrokerMetricModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerMetric, value: Double): Double = roundTo(value, step)
}

data class MinimumBrokerMetricAdjuster(var minimum: Double) : BrokerMetricModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BrokerMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBrokerMetricAdjuster(var percent: Double) : BrokerMetricModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the broker metric steps in order and keeps a trace of each. */
data class BrokerMetricEngine(
    var steps: MutableList<BrokerMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BrokerMetric): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BrokerMetricModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BrokerMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBrokerMetricEngine(flags: Registry): BrokerMetricEngine {
    return BrokerMetricEngine(
        steps = mutableListOf<BrokerMetricModifier>(
            CapBrokerMetricAdjuster(1250.0),
            FlaggedBrokerMetricAdjuster(flags, "broker-metric-strict", 6.8),
            SegmentBrokerMetricAdjuster(mutableMapOf<String, Double>("south" to 1.08, "online" to 0.99)),
            RoundingBrokerMetricAdjuster(1.0),
            MinimumBrokerMetricAdjuster(39.7),
            PercentBrokerMetricAdjuster(2.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 20196.7,
    )
}
