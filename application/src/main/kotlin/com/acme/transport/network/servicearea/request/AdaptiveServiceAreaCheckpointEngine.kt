package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive service area checkpoint calculation. */
interface AdaptiveServiceAreaCheckpointFactor {
    fun describe(): String
    fun adjust(item: AdaptiveServiceAreaCheckpoint, value: Double): Double
}

data class CapAdaptiveServiceAreaCheckpointAdjuster(var cap: Double) : AdaptiveServiceAreaCheckpointFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveServiceAreaCheckpoint, value: Double): Double = minOf(value, cap)
}

data class FlaggedAdaptiveServiceAreaCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveServiceAreaCheckpointFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveServiceAreaCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAdaptiveServiceAreaCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveServiceAreaCheckpointFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveServiceAreaCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAdaptiveServiceAreaCheckpointAdjuster(
    var percent: Double,
) : AdaptiveServiceAreaCheckpointFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveServiceAreaCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AdaptiveServiceAreaCheckpointEndsAtOffsetAdjuster(
    var weight: Double,
) : AdaptiveServiceAreaCheckpointFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: AdaptiveServiceAreaCheckpoint, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

data class MinimumAdaptiveServiceAreaCheckpointAdjuster(
    var minimum: Double,
) : AdaptiveServiceAreaCheckpointFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveServiceAreaCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive service area checkpoint steps in order and keeps a trace of each. */
data class AdaptiveServiceAreaCheckpointEngine(
    var steps: MutableList<AdaptiveServiceAreaCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveServiceAreaCheckpoint): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveServiceAreaCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveServiceAreaCheckpointEngine(flags: Registry): AdaptiveServiceAreaCheckpointEngine {
    return AdaptiveServiceAreaCheckpointEngine(
        steps = mutableListOf<AdaptiveServiceAreaCheckpointFactor>(
            CapAdaptiveServiceAreaCheckpointAdjuster(13299.0),
            FlaggedAdaptiveServiceAreaCheckpointAdjuster(
                flags,
                "adaptive-service-area-checkpoint-primary",
                -6.1,
            ),
            TieredAdaptiveServiceAreaCheckpointAdjuster(
                mutableListOf<Double>(590.0, 2517.0, 3491.0, 6847.0),
                mutableListOf<Double>(1.04, 0.82, 1.19, 1.17),
            ),
            PercentAdaptiveServiceAreaCheckpointAdjuster(-12.0),
            AdaptiveServiceAreaCheckpointEndsAtOffsetAdjuster(0.66),
            MinimumAdaptiveServiceAreaCheckpointAdjuster(43.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 92970.9,
    )
}
