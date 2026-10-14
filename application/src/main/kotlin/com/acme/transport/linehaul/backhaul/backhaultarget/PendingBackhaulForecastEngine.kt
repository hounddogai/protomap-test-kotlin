package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending backhaul forecast calculation. */
interface PendingBackhaulForecastFactor {
    fun describe(): String
    fun adjust(item: PendingBackhaulForecast, value: Double): Double
}

data class RegionPendingBackhaulForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingBackhaulForecastFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: PendingBackhaulForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredPendingBackhaulForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingBackhaulForecastFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingBackhaulForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPendingBackhaulForecastAdjuster(var step: Double) : PendingBackhaulForecastFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingBackhaulForecast, value: Double): Double = roundTo(value, step)
}

data class PercentPendingBackhaulForecastAdjuster(var percent: Double) : PendingBackhaulForecastFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingBackhaulForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PendingBackhaulForecastStopCountOffsetAdjuster(var weight: Double) : PendingBackhaulForecastFactor {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: PendingBackhaulForecast, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

/** Runs the pending backhaul forecast steps in order and keeps a trace of each. */
data class PendingBackhaulForecastEngine(
    var steps: MutableList<PendingBackhaulForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingBackhaulForecast): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingBackhaulForecastFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingBackhaulForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingBackhaulForecastEngine(): PendingBackhaulForecastEngine {
    return PendingBackhaulForecastEngine(
        steps = mutableListOf<PendingBackhaulForecastFactor>(
            RegionPendingBackhaulForecastAdjuster(
                mutableMapOf<String, Double>("south" to 0.76, "north" to 1.09),
            ),
            TieredPendingBackhaulForecastAdjuster(
                mutableListOf<Double>(1378.0, 3171.0, 6640.0, 7846.0),
                mutableListOf<Double>(0.84, 0.82, 1.01, 1.18),
            ),
            RoundingPendingBackhaulForecastAdjuster(0.5),
            PercentPendingBackhaulForecastAdjuster(10.8),
            PendingBackhaulForecastStopCountOffsetAdjuster(0.28),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 59385.7,
    )
}
