package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback idling manifest calculation. */
interface FallbackIdlingManifestStep {
    fun describe(): String
    fun adjust(item: FallbackIdlingManifest, value: Double): Double
}

data class RoundingFallbackIdlingManifestAdjuster(var step: Double) : FallbackIdlingManifestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackIdlingManifest, value: Double): Double = roundTo(value, step)
}

data class MinimumFallbackIdlingManifestAdjuster(var minimum: Double) : FallbackIdlingManifestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackIdlingManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredFallbackIdlingManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackIdlingManifestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackIdlingManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedFallbackIdlingManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackIdlingManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackIdlingManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackIdlingManifestAdjuster(var cap: Double) : FallbackIdlingManifestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackIdlingManifest, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackIdlingManifestAdjuster(var percent: Double) : FallbackIdlingManifestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackIdlingManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback idling manifest steps in order and keeps a trace of each. */
data class FallbackIdlingManifestEngine(
    var steps: MutableList<FallbackIdlingManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackIdlingManifest): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackIdlingManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackIdlingManifestEngine(flags: Registry): FallbackIdlingManifestEngine {
    return FallbackIdlingManifestEngine(
        steps = mutableListOf<FallbackIdlingManifestStep>(
            RoundingFallbackIdlingManifestAdjuster(1.0),
            MinimumFallbackIdlingManifestAdjuster(42.6),
            TieredFallbackIdlingManifestAdjuster(
                mutableListOf<Double>(719.0, 2039.0, 8874.0),
                mutableListOf<Double>(0.84, 0.97, 1.14),
            ),
            FlaggedFallbackIdlingManifestAdjuster(flags, "fallback-idling-manifest-scheduled", 2.8),
            CapFallbackIdlingManifestAdjuster(39722.0),
            PercentFallbackIdlingManifestAdjuster(24.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 29233.2,
    )
}
