package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped dock door threshold calculation. */
interface CappedDockDoorThresholdModifier {
    fun describe(): String
    fun adjust(item: CappedDockDoorThreshold, value: Double): Double
}

data class TieredCappedDockDoorThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedDockDoorThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedDockDoorThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCappedDockDoorThresholdAdjuster(var minimum: Double) : CappedDockDoorThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedDockDoorThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCappedDockDoorThresholdAdjuster(var percent: Double) : CappedDockDoorThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedDockDoorThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped dock door threshold steps in order and keeps a trace of each. */
data class CappedDockDoorThresholdEngine(
    var steps: MutableList<CappedDockDoorThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedDockDoorThreshold): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedDockDoorThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedDockDoorThresholdEngine(): CappedDockDoorThresholdEngine {
    return CappedDockDoorThresholdEngine(
        steps = mutableListOf<CappedDockDoorThresholdModifier>(
            TieredCappedDockDoorThresholdAdjuster(
                mutableListOf<Double>(358.0, 1390.0, 1429.0),
                mutableListOf<Double>(1.07, 0.99, 0.85),
            ),
            MinimumCappedDockDoorThresholdAdjuster(24.9),
            PercentCappedDockDoorThresholdAdjuster(5.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 22271.4,
    )
}
