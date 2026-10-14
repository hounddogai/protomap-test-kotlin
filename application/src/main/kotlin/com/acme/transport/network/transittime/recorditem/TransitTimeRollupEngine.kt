package com.acme.transport.network.transittime.recorditem

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the transit time rollup calculation. */
interface TransitTimeRollupFactor {
    fun describe(): String
    fun adjust(item: TransitTimeRollup, value: Double): Double
}

data class MinimumTransitTimeRollupAdjuster(var minimum: Double) : TransitTimeRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelTransitTimeRollupAdjuster(var factors: MutableMap<String, Double>) : TransitTimeRollupFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: TransitTimeRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTransitTimeRollupAdjuster(var step: Double) : TransitTimeRollupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimeRollup, value: Double): Double = roundTo(value, step)
}

data class CapTransitTimeRollupAdjuster(var cap: Double) : TransitTimeRollupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimeRollup, value: Double): Double = minOf(value, cap)
}

data class PercentTransitTimeRollupAdjuster(var percent: Double) : TransitTimeRollupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimeRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the transit time rollup steps in order and keeps a trace of each. */
data class TransitTimeRollupEngine(
    var steps: MutableList<TransitTimeRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TransitTimeRollup): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TransitTimeRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTransitTimeRollupEngine(): TransitTimeRollupEngine {
    return TransitTimeRollupEngine(
        steps = mutableListOf<TransitTimeRollupFactor>(
            MinimumTransitTimeRollupAdjuster(1.8),
            LabelTransitTimeRollupAdjuster(mutableMapOf<String, Double>("south" to 0.7)),
            RoundingTransitTimeRollupAdjuster(5.0),
            CapTransitTimeRollupAdjuster(17868.0),
            PercentTransitTimeRollupAdjuster(4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 57293.5,
    )
}
