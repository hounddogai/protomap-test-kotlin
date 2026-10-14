package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending relay forecast calculation. */
interface PendingRelayForecastModifier {
    fun describe(): String
    fun adjust(item: PendingRelayForecast, value: Double): Double
}

data class PendingRelayForecastRevisionOffsetAdjuster(var weight: Double) : PendingRelayForecastModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: PendingRelayForecast, value: Double): Double = value + item.revision.toDouble() * weight
}

data class CapPendingRelayForecastAdjuster(var cap: Double) : PendingRelayForecastModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingRelayForecast, value: Double): Double = minOf(value, cap)
}

data class RoundingPendingRelayForecastAdjuster(var step: Double) : PendingRelayForecastModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingRelayForecast, value: Double): Double = roundTo(value, step)
}

data class TieredPendingRelayForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingRelayForecastModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingRelayForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPendingRelayForecastAdjuster(var minimum: Double) : PendingRelayForecastModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingRelayForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending relay forecast steps in order and keeps a trace of each. */
data class PendingRelayForecastEngine(
    var steps: MutableList<PendingRelayForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingRelayForecast): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingRelayForecastModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingRelayForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingRelayForecastEngine(): PendingRelayForecastEngine {
    return PendingRelayForecastEngine(
        steps = mutableListOf<PendingRelayForecastModifier>(
            PendingRelayForecastRevisionOffsetAdjuster(1.05),
            CapPendingRelayForecastAdjuster(9396.0),
            RoundingPendingRelayForecastAdjuster(0.1),
            TieredPendingRelayForecastAdjuster(
                mutableListOf<Double>(1176.0, 6806.0),
                mutableListOf<Double>(0.89, 1.07),
            ),
            MinimumPendingRelayForecastAdjuster(45.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 41792.3,
    )
}
