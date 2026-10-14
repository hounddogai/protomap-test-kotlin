package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted trailer threshold calculation. */
interface WeightedTrailerThresholdModifier {
    fun describe(): String
    fun adjust(item: WeightedTrailerThreshold, value: Double): Double
}

data class CapWeightedTrailerThresholdAdjuster(var cap: Double) : WeightedTrailerThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedTrailerThreshold, value: Double): Double = minOf(value, cap)
}

data class FlaggedWeightedTrailerThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedTrailerThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedTrailerThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class WeightedTrailerThresholdMarginOffsetAdjuster(var weight: Double) : WeightedTrailerThresholdModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: WeightedTrailerThreshold, value: Double): Double = value + item.margin * weight
}

data class TieredWeightedTrailerThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedTrailerThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedTrailerThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted trailer threshold steps in order and keeps a trace of each. */
data class WeightedTrailerThresholdEngine(
    var steps: MutableList<WeightedTrailerThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedTrailerThreshold): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedTrailerThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedTrailerThresholdEngine(flags: Registry): WeightedTrailerThresholdEngine {
    return WeightedTrailerThresholdEngine(
        steps = mutableListOf<WeightedTrailerThresholdModifier>(
            CapWeightedTrailerThresholdAdjuster(3062.0),
            FlaggedWeightedTrailerThresholdAdjuster(flags, "weighted-trailer-threshold-lenient", 9.4),
            WeightedTrailerThresholdMarginOffsetAdjuster(0.18),
            TieredWeightedTrailerThresholdAdjuster(
                mutableListOf<Double>(113.0, 2300.0, 5121.0, 6051.0),
                mutableListOf<Double>(0.82, 1.1, 0.91, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 26582.3,
    )
}
