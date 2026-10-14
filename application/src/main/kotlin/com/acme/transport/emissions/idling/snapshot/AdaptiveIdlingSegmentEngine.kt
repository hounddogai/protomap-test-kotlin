package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive idling segment calculation. */
interface AdaptiveIdlingSegmentAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveIdlingSegment, value: Double): Double
}

data class PercentAdaptiveIdlingSegmentAdjuster(var percent: Double) : AdaptiveIdlingSegmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveIdlingSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAdaptiveIdlingSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveIdlingSegmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveIdlingSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAdaptiveIdlingSegmentAdjuster(var minimum: Double) : AdaptiveIdlingSegmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveIdlingSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAdaptiveIdlingSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveIdlingSegmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveIdlingSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryAdaptiveIdlingSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveIdlingSegmentAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: AdaptiveIdlingSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAdaptiveIdlingSegmentAdjuster(var step: Double) : AdaptiveIdlingSegmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveIdlingSegment, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive idling segment steps in order and keeps a trace of each. */
data class AdaptiveIdlingSegmentEngine(
    var steps: MutableList<AdaptiveIdlingSegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveIdlingSegment): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveIdlingSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveIdlingSegmentEngine(flags: Registry): AdaptiveIdlingSegmentEngine {
    return AdaptiveIdlingSegmentEngine(
        steps = mutableListOf<AdaptiveIdlingSegmentAdjuster>(
            PercentAdaptiveIdlingSegmentAdjuster(-8.6),
            FlaggedAdaptiveIdlingSegmentAdjuster(flags, "adaptive-idling-segment-primary", -2.1),
            MinimumAdaptiveIdlingSegmentAdjuster(27.9),
            TieredAdaptiveIdlingSegmentAdjuster(
                mutableListOf<Double>(3775.0, 5560.0, 5792.0, 9797.0),
                mutableListOf<Double>(1.08, 0.82, 0.93, 0.89),
            ),
            CategoryAdaptiveIdlingSegmentAdjuster(
                mutableMapOf<String, Double>("west" to 1.33, "south" to 1.25, "central" to 0.93),
            ),
            RoundingAdaptiveIdlingSegmentAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 68390.9,
    )
}
