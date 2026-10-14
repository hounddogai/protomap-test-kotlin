package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive trailer yard rollup calculation. */
interface AdaptiveTrailerYardRollupAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveTrailerYardRollup, value: Double): Double
}

data class UnitAdaptiveTrailerYardRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveTrailerYardRollupAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: AdaptiveTrailerYardRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAdaptiveTrailerYardRollupAdjuster(var cap: Double) : AdaptiveTrailerYardRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveTrailerYardRollup, value: Double): Double = minOf(value, cap)
}

data class PercentAdaptiveTrailerYardRollupAdjuster(var percent: Double) : AdaptiveTrailerYardRollupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveTrailerYardRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingAdaptiveTrailerYardRollupAdjuster(var step: Double) : AdaptiveTrailerYardRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveTrailerYardRollup, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive trailer yard rollup steps in order and keeps a trace of each. */
data class AdaptiveTrailerYardRollupEngine(
    var steps: MutableList<AdaptiveTrailerYardRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveTrailerYardRollup): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveTrailerYardRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveTrailerYardRollupEngine(): AdaptiveTrailerYardRollupEngine {
    return AdaptiveTrailerYardRollupEngine(
        steps = mutableListOf<AdaptiveTrailerYardRollupAdjuster>(
            UnitAdaptiveTrailerYardRollupAdjuster(mutableMapOf<String, Double>("north" to 1.34)),
            CapAdaptiveTrailerYardRollupAdjuster(35511.0),
            PercentAdaptiveTrailerYardRollupAdjuster(11.9),
            RoundingAdaptiveTrailerYardRollupAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 81003.5,
    )
}
