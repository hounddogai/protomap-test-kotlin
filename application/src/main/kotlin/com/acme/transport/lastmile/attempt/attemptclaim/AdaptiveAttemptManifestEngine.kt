package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive attempt manifest calculation. */
interface AdaptiveAttemptManifestStep {
    fun describe(): String
    fun adjust(item: AdaptiveAttemptManifest, value: Double): Double
}

data class FlaggedAdaptiveAttemptManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveAttemptManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveAttemptManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAdaptiveAttemptManifestAdjuster(var percent: Double) : AdaptiveAttemptManifestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveAttemptManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAdaptiveAttemptManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveAttemptManifestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveAttemptManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAdaptiveAttemptManifestAdjuster(var minimum: Double) : AdaptiveAttemptManifestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveAttemptManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAdaptiveAttemptManifestAdjuster(var cap: Double) : AdaptiveAttemptManifestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveAttemptManifest, value: Double): Double = minOf(value, cap)
}

data class AdaptiveAttemptManifestWindowDaysOffsetAdjuster(var weight: Double) : AdaptiveAttemptManifestStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: AdaptiveAttemptManifest, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

/** Runs the adaptive attempt manifest steps in order and keeps a trace of each. */
data class AdaptiveAttemptManifestEngine(
    var steps: MutableList<AdaptiveAttemptManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveAttemptManifest): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveAttemptManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveAttemptManifestEngine(flags: Registry): AdaptiveAttemptManifestEngine {
    return AdaptiveAttemptManifestEngine(
        steps = mutableListOf<AdaptiveAttemptManifestStep>(
            FlaggedAdaptiveAttemptManifestAdjuster(flags, "adaptive-attempt-manifest-global", -6.1),
            PercentAdaptiveAttemptManifestAdjuster(14.6),
            TieredAdaptiveAttemptManifestAdjuster(
                mutableListOf<Double>(1838.0, 4541.0, 8283.0, 8830.0),
                mutableListOf<Double>(1.19, 0.91, 1.16, 0.95),
            ),
            MinimumAdaptiveAttemptManifestAdjuster(26.3),
            CapAdaptiveAttemptManifestAdjuster(40138.0),
            AdaptiveAttemptManifestWindowDaysOffsetAdjuster(1.36),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 50816.4,
    )
}
