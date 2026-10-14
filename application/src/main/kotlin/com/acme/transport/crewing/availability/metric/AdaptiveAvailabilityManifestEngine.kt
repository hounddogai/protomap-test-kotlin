package com.acme.transport.crewing.availability.metric

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive availability manifest calculation. */
interface AdaptiveAvailabilityManifestAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveAvailabilityManifest, value: Double): Double
}

data class LabelAdaptiveAvailabilityManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveAvailabilityManifestAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: AdaptiveAvailabilityManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AdaptiveAvailabilityManifestScoreOffsetAdjuster(
    var weight: Double,
) : AdaptiveAvailabilityManifestAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: AdaptiveAvailabilityManifest, value: Double): Double = value + item.score * weight
}

data class PercentAdaptiveAvailabilityManifestAdjuster(
    var percent: Double,
) : AdaptiveAvailabilityManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveAvailabilityManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAdaptiveAvailabilityManifestAdjuster(var cap: Double) : AdaptiveAvailabilityManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveAvailabilityManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive availability manifest steps in order and keeps a trace of each. */
data class AdaptiveAvailabilityManifestEngine(
    var steps: MutableList<AdaptiveAvailabilityManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveAvailabilityManifest): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveAvailabilityManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveAvailabilityManifestEngine(): AdaptiveAvailabilityManifestEngine {
    return AdaptiveAvailabilityManifestEngine(
        steps = mutableListOf<AdaptiveAvailabilityManifestAdjuster>(
            LabelAdaptiveAvailabilityManifestAdjuster(mutableMapOf<String, Double>("east" to 0.78)),
            AdaptiveAvailabilityManifestScoreOffsetAdjuster(1.89),
            PercentAdaptiveAvailabilityManifestAdjuster(12.0),
            CapAdaptiveAvailabilityManifestAdjuster(38077.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 5516.0,
    )
}
