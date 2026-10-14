package com.acme.transport.customs.broker.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the broker variance calculation. */
interface BrokerVarianceModifier {
    fun describe(): String
    fun adjust(item: BrokerVariance, value: Double): Double
}

data class TitleBrokerVarianceAdjuster(var factors: MutableMap<String, Double>) : BrokerVarianceModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: BrokerVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BrokerVarianceQuantityOffsetAdjuster(var weight: Double) : BrokerVarianceModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: BrokerVariance, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class FlaggedBrokerVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BrokerVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BrokerVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBrokerVarianceAdjuster(var percent: Double) : BrokerVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredBrokerVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BrokerVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BrokerVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingBrokerVarianceAdjuster(var step: Double) : BrokerVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerVariance, value: Double): Double = roundTo(value, step)
}

/** Runs the broker variance steps in order and keeps a trace of each. */
data class BrokerVarianceEngine(
    var steps: MutableList<BrokerVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BrokerVariance): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BrokerVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBrokerVarianceEngine(flags: Registry): BrokerVarianceEngine {
    return BrokerVarianceEngine(
        steps = mutableListOf<BrokerVarianceModifier>(
            TitleBrokerVarianceAdjuster(mutableMapOf<String, Double>("east" to 1.0, "west" to 1.33)),
            BrokerVarianceQuantityOffsetAdjuster(1.85),
            FlaggedBrokerVarianceAdjuster(flags, "broker-variance-seasonal", 2.9),
            PercentBrokerVarianceAdjuster(13.7),
            TieredBrokerVarianceAdjuster(
                mutableListOf<Double>(4575.0, 6441.0, 7114.0, 8066.0),
                mutableListOf<Double>(1.2, 0.96, 0.83, 0.85),
            ),
            RoundingBrokerVarianceAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 73850.4,
    )
}
