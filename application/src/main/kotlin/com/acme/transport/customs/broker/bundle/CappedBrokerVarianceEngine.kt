package com.acme.transport.customs.broker.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped broker variance calculation. */
interface CappedBrokerVarianceAdjuster {
    fun describe(): String
    fun adjust(item: CappedBrokerVariance, value: Double): Double
}

data class CapCappedBrokerVarianceAdjuster(var cap: Double) : CappedBrokerVarianceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedBrokerVariance, value: Double): Double = minOf(value, cap)
}

data class PercentCappedBrokerVarianceAdjuster(var percent: Double) : CappedBrokerVarianceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedBrokerVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCappedBrokerVarianceAdjuster(var minimum: Double) : CappedBrokerVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedBrokerVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedBrokerVarianceAdjuster(var step: Double) : CappedBrokerVarianceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedBrokerVariance, value: Double): Double = roundTo(value, step)
}

data class CappedBrokerVarianceGraceDaysOffsetAdjuster(var weight: Double) : CappedBrokerVarianceAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: CappedBrokerVariance, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class FlaggedCappedBrokerVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedBrokerVarianceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedBrokerVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped broker variance steps in order and keeps a trace of each. */
data class CappedBrokerVarianceEngine(
    var steps: MutableList<CappedBrokerVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedBrokerVariance): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedBrokerVarianceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CappedBrokerVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedBrokerVarianceEngine(flags: Registry): CappedBrokerVarianceEngine {
    return CappedBrokerVarianceEngine(
        steps = mutableListOf<CappedBrokerVarianceAdjuster>(
            CapCappedBrokerVarianceAdjuster(2688.0),
            PercentCappedBrokerVarianceAdjuster(14.1),
            MinimumCappedBrokerVarianceAdjuster(9.6),
            RoundingCappedBrokerVarianceAdjuster(5.0),
            CappedBrokerVarianceGraceDaysOffsetAdjuster(1.42),
            FlaggedCappedBrokerVarianceAdjuster(flags, "capped-broker-variance-lenient", -1.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 5381.3,
    )
}
