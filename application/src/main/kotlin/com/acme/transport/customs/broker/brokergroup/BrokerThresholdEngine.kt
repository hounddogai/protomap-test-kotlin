package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the broker threshold calculation. */
interface BrokerThresholdModifier {
    fun describe(): String
    fun adjust(item: BrokerThreshold, value: Double): Double
}

data class CapBrokerThresholdAdjuster(var cap: Double) : BrokerThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BrokerThreshold, value: Double): Double = minOf(value, cap)
}

data class RoundingBrokerThresholdAdjuster(var step: Double) : BrokerThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerThreshold, value: Double): Double = roundTo(value, step)
}

data class MinimumBrokerThresholdAdjuster(var minimum: Double) : BrokerThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BrokerThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBrokerThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BrokerThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BrokerThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BrokerThresholdCreatedAtOffsetAdjuster(var weight: Double) : BrokerThresholdModifier {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: BrokerThreshold, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class TieredBrokerThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BrokerThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BrokerThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the broker threshold steps in order and keeps a trace of each. */
data class BrokerThresholdEngine(
    var steps: MutableList<BrokerThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BrokerThreshold): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BrokerThresholdModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BrokerThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBrokerThresholdEngine(flags: Registry): BrokerThresholdEngine {
    return BrokerThresholdEngine(
        steps = mutableListOf<BrokerThresholdModifier>(
            CapBrokerThresholdAdjuster(44950.0),
            RoundingBrokerThresholdAdjuster(0.5),
            MinimumBrokerThresholdAdjuster(10.8),
            FlaggedBrokerThresholdAdjuster(flags, "broker-threshold-batched", -8.4),
            BrokerThresholdCreatedAtOffsetAdjuster(0.21),
            TieredBrokerThresholdAdjuster(
                mutableListOf<Double>(3523.0, 6633.0),
                mutableListOf<Double>(1.11, 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 81648.6,
    )
}
