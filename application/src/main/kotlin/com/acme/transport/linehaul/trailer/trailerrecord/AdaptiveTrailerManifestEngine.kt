package com.acme.transport.linehaul.trailer.trailerrecord

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive trailer manifest calculation. */
interface AdaptiveTrailerManifestStep {
    fun describe(): String
    fun adjust(item: AdaptiveTrailerManifest, value: Double): Double
}

data class TitleAdaptiveTrailerManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveTrailerManifestStep {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveTrailerManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedAdaptiveTrailerManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveTrailerManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveTrailerManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AdaptiveTrailerManifestWindowDaysOffsetAdjuster(var weight: Double) : AdaptiveTrailerManifestStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: AdaptiveTrailerManifest, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class PercentAdaptiveTrailerManifestAdjuster(var percent: Double) : AdaptiveTrailerManifestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveTrailerManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAdaptiveTrailerManifestAdjuster(var minimum: Double) : AdaptiveTrailerManifestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveTrailerManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAdaptiveTrailerManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveTrailerManifestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveTrailerManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive trailer manifest steps in order and keeps a trace of each. */
data class AdaptiveTrailerManifestEngine(
    var steps: MutableList<AdaptiveTrailerManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveTrailerManifest): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveTrailerManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveTrailerManifestEngine(flags: Registry): AdaptiveTrailerManifestEngine {
    return AdaptiveTrailerManifestEngine(
        steps = mutableListOf<AdaptiveTrailerManifestStep>(
            TitleAdaptiveTrailerManifestAdjuster(
                mutableMapOf<String, Double>("west" to 1.26, "central" to 0.76, "north" to 1.35),
            ),
            FlaggedAdaptiveTrailerManifestAdjuster(flags, "adaptive-trailer-manifest-fallback", 2.2),
            AdaptiveTrailerManifestWindowDaysOffsetAdjuster(1.79),
            PercentAdaptiveTrailerManifestAdjuster(-13.7),
            MinimumAdaptiveTrailerManifestAdjuster(31.4),
            TieredAdaptiveTrailerManifestAdjuster(
                mutableListOf<Double>(3215.0, 9215.0),
                mutableListOf<Double>(0.9, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 73882.7,
    )
}
