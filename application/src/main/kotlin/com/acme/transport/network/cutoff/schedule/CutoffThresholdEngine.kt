package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cutoff threshold calculation. */
interface CutoffThresholdModifier {
    fun describe(): String
    fun adjust(item: CutoffThreshold, value: Double): Double
}

data class RoundingCutoffThresholdAdjuster(var step: Double) : CutoffThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffThreshold, value: Double): Double = roundTo(value, step)
}

data class MinimumCutoffThresholdAdjuster(var minimum: Double) : CutoffThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CutoffThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCutoffThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CutoffThresholdThresholdOffsetAdjuster(var weight: Double) : CutoffThresholdModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: CutoffThreshold, value: Double): Double = value + item.threshold * weight
}

data class CapCutoffThresholdAdjuster(var cap: Double) : CutoffThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffThreshold, value: Double): Double = minOf(value, cap)
}

data class TieredCutoffThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CutoffThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CutoffThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the cutoff threshold steps in order and keeps a trace of each. */
data class CutoffThresholdEngine(
    var steps: MutableList<CutoffThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CutoffThreshold): Double {
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

    fun evaluateAll(items: MutableList<CutoffThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCutoffThresholdEngine(flags: Registry): CutoffThresholdEngine {
    return CutoffThresholdEngine(
        steps = mutableListOf<CutoffThresholdModifier>(
            RoundingCutoffThresholdAdjuster(0.1),
            MinimumCutoffThresholdAdjuster(24.3),
            FlaggedCutoffThresholdAdjuster(flags, "cutoff-threshold-default", 2.8),
            CutoffThresholdThresholdOffsetAdjuster(1.29),
            CapCutoffThresholdAdjuster(25841.0),
            TieredCutoffThresholdAdjuster(
                mutableListOf<Double>(792.0, 2030.0, 2092.0, 2519.0),
                mutableListOf<Double>(1.05, 1.12, 1.02, 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 64639.9,
    )
}
