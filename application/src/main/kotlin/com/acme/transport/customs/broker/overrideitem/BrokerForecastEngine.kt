package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the broker forecast calculation. */
interface BrokerForecastFactor {
    fun describe(): String
    fun adjust(item: BrokerForecast, value: Double): Double
}

data class CapBrokerForecastAdjuster(var cap: Double) : BrokerForecastFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BrokerForecast, value: Double): Double = minOf(value, cap)
}

data class FlaggedBrokerForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BrokerForecastFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BrokerForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBrokerForecastAdjuster(var step: Double) : BrokerForecastFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerForecast, value: Double): Double = roundTo(value, step)
}

data class PercentBrokerForecastAdjuster(var percent: Double) : BrokerForecastFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the broker forecast steps in order and keeps a trace of each. */
data class BrokerForecastEngine(
    var steps: MutableList<BrokerForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BrokerForecast): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BrokerForecastFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BrokerForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBrokerForecastEngine(flags: Registry): BrokerForecastEngine {
    return BrokerForecastEngine(
        steps = mutableListOf<BrokerForecastFactor>(
            CapBrokerForecastAdjuster(27433.0),
            FlaggedBrokerForecastAdjuster(flags, "broker-forecast-strict", -7.2),
            RoundingBrokerForecastAdjuster(0.5),
            PercentBrokerForecastAdjuster(-15.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 24557.9,
    )
}
