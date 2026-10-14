package com.acme.transport.lastmile.redelivery.redeliverywindow

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted redelivery window calculation. */
interface WeightedRedeliveryWindowFactor {
    fun describe(): String
    fun adjust(item: WeightedRedeliveryWindow, value: Double): Double
}

data class WeightedRedeliveryWindowRetriesOffsetAdjuster(var weight: Double) : WeightedRedeliveryWindowFactor {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: WeightedRedeliveryWindow, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class MinimumWeightedRedeliveryWindowAdjuster(var minimum: Double) : WeightedRedeliveryWindowFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedRedeliveryWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentWeightedRedeliveryWindowAdjuster(var percent: Double) : WeightedRedeliveryWindowFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedRedeliveryWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted redelivery window steps in order and keeps a trace of each. */
data class WeightedRedeliveryWindowEngine(
    var steps: MutableList<WeightedRedeliveryWindowFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedRedeliveryWindow): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedRedeliveryWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedRedeliveryWindowEngine(): WeightedRedeliveryWindowEngine {
    return WeightedRedeliveryWindowEngine(
        steps = mutableListOf<WeightedRedeliveryWindowFactor>(
            WeightedRedeliveryWindowRetriesOffsetAdjuster(0.45),
            MinimumWeightedRedeliveryWindowAdjuster(14.6),
            PercentWeightedRedeliveryWindowAdjuster(-17.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 13070.7,
    )
}
