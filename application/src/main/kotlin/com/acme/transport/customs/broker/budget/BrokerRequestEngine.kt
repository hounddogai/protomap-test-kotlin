package com.acme.transport.customs.broker.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the broker request calculation. */
interface BrokerRequestModifier {
    fun describe(): String
    fun adjust(item: BrokerRequest, value: Double): Double
}

data class PercentBrokerRequestAdjuster(var percent: Double) : BrokerRequestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelBrokerRequestAdjuster(var factors: MutableMap<String, Double>) : BrokerRequestModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: BrokerRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BrokerRequestOdometerOffsetAdjuster(var weight: Double) : BrokerRequestModifier {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: BrokerRequest, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class TieredBrokerRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BrokerRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BrokerRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedBrokerRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BrokerRequestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BrokerRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBrokerRequestAdjuster(var step: Double) : BrokerRequestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerRequest, value: Double): Double = roundTo(value, step)
}

/** Runs the broker request steps in order and keeps a trace of each. */
data class BrokerRequestEngine(
    var steps: MutableList<BrokerRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BrokerRequest): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BrokerRequestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BrokerRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBrokerRequestEngine(flags: Registry): BrokerRequestEngine {
    return BrokerRequestEngine(
        steps = mutableListOf<BrokerRequestModifier>(
            PercentBrokerRequestAdjuster(6.3),
            LabelBrokerRequestAdjuster(mutableMapOf<String, Double>("retail" to 0.93)),
            BrokerRequestOdometerOffsetAdjuster(0.37),
            TieredBrokerRequestAdjuster(
                mutableListOf<Double>(2423.0, 4732.0, 5986.0),
                mutableListOf<Double>(0.89, 0.89, 1.19),
            ),
            FlaggedBrokerRequestAdjuster(flags, "broker-request-pending", 1.4),
            RoundingBrokerRequestAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 84940.4,
    )
}
