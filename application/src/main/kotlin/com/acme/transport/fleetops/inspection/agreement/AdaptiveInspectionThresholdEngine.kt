package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive inspection threshold calculation. */
interface AdaptiveInspectionThresholdAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveInspectionThreshold, value: Double): Double
}

data class MinimumAdaptiveInspectionThresholdAdjuster(
    var minimum: Double,
) : AdaptiveInspectionThresholdAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveInspectionThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAdaptiveInspectionThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveInspectionThresholdAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveInspectionThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAdaptiveInspectionThresholdAdjuster(var cap: Double) : AdaptiveInspectionThresholdAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveInspectionThreshold, value: Double): Double = minOf(value, cap)
}

data class RoundingAdaptiveInspectionThresholdAdjuster(var step: Double) : AdaptiveInspectionThresholdAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveInspectionThreshold, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive inspection threshold steps in order and keeps a trace of each. */
data class AdaptiveInspectionThresholdEngine(
    var steps: MutableList<AdaptiveInspectionThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveInspectionThreshold): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveInspectionThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveInspectionThresholdEngine(): AdaptiveInspectionThresholdEngine {
    return AdaptiveInspectionThresholdEngine(
        steps = mutableListOf<AdaptiveInspectionThresholdAdjuster>(
            MinimumAdaptiveInspectionThresholdAdjuster(19.8),
            TieredAdaptiveInspectionThresholdAdjuster(
                mutableListOf<Double>(3464.0, 3831.0, 8299.0),
                mutableListOf<Double>(0.85, 0.91, 0.9),
            ),
            CapAdaptiveInspectionThresholdAdjuster(44937.0),
            RoundingAdaptiveInspectionThresholdAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 64962.9,
    )
}
