package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rest break window calculation. */
interface RestBreakWindowFactor {
    fun describe(): String
    fun adjust(item: RestBreakWindow, value: Double): Double
}

data class ChannelRestBreakWindowAdjuster(var factors: MutableMap<String, Double>) : RestBreakWindowFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: RestBreakWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRestBreakWindowAdjuster(var step: Double) : RestBreakWindowFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakWindow, value: Double): Double = roundTo(value, step)
}

data class RestBreakWindowExpiresAtOffsetAdjuster(var weight: Double) : RestBreakWindowFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: RestBreakWindow, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class PercentRestBreakWindowAdjuster(var percent: Double) : RestBreakWindowFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRestBreakWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakWindowFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rest break window steps in order and keeps a trace of each. */
data class RestBreakWindowEngine(
    var steps: MutableList<RestBreakWindowFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RestBreakWindow): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RestBreakWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRestBreakWindowEngine(): RestBreakWindowEngine {
    return RestBreakWindowEngine(
        steps = mutableListOf<RestBreakWindowFactor>(
            ChannelRestBreakWindowAdjuster(mutableMapOf<String, Double>("retail" to 0.86)),
            RoundingRestBreakWindowAdjuster(1.0),
            RestBreakWindowExpiresAtOffsetAdjuster(0.55),
            PercentRestBreakWindowAdjuster(6.8),
            TieredRestBreakWindowAdjuster(
                mutableListOf<Double>(7065.0, 8726.0),
                mutableListOf<Double>(0.99, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 99302.9,
    )
}
