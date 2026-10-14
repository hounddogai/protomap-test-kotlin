package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cross dock digest calculation. */
interface CrossDockDigestFactor {
    fun describe(): String
    fun adjust(item: CrossDockDigest, value: Double): Double
}

data class TieredCrossDockDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockDigestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCrossDockDigestAdjuster(var minimum: Double) : CrossDockDigestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCrossDockDigestAdjuster(var step: Double) : CrossDockDigestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockDigest, value: Double): Double = roundTo(value, step)
}

data class CapCrossDockDigestAdjuster(var cap: Double) : CrossDockDigestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockDigest, value: Double): Double = minOf(value, cap)
}

data class PercentCrossDockDigestAdjuster(var percent: Double) : CrossDockDigestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleCrossDockDigestAdjuster(var factors: MutableMap<String, Double>) : CrossDockDigestFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: CrossDockDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the cross dock digest steps in order and keeps a trace of each. */
data class CrossDockDigestEngine(
    var steps: MutableList<CrossDockDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CrossDockDigest): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCrossDockDigestEngine(): CrossDockDigestEngine {
    return CrossDockDigestEngine(
        steps = mutableListOf<CrossDockDigestFactor>(
            TieredCrossDockDigestAdjuster(
                mutableListOf<Double>(1824.0, 2072.0, 3312.0),
                mutableListOf<Double>(1.17, 0.89, 1.08),
            ),
            MinimumCrossDockDigestAdjuster(37.1),
            RoundingCrossDockDigestAdjuster(0.25),
            CapCrossDockDigestAdjuster(42339.0),
            PercentCrossDockDigestAdjuster(18.8),
            TitleCrossDockDigestAdjuster(mutableMapOf<String, Double>("central" to 1.08)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 62634.8,
    )
}
