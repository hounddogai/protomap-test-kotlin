package com.acme.transport.network.cutoff.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback cutoff variance calculation. */
interface FallbackCutoffVarianceStep {
    fun describe(): String
    fun adjust(item: FallbackCutoffVariance, value: Double): Double
}

data class CapFallbackCutoffVarianceAdjuster(var cap: Double) : FallbackCutoffVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackCutoffVariance, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackCutoffVarianceAdjuster(var percent: Double) : FallbackCutoffVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackCutoffVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedFallbackCutoffVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackCutoffVarianceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackCutoffVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredFallbackCutoffVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackCutoffVarianceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackCutoffVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumFallbackCutoffVarianceAdjuster(var minimum: Double) : FallbackCutoffVarianceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackCutoffVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFallbackCutoffVarianceAdjuster(var step: Double) : FallbackCutoffVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackCutoffVariance, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback cutoff variance steps in order and keeps a trace of each. */
data class FallbackCutoffVarianceEngine(
    var steps: MutableList<FallbackCutoffVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackCutoffVariance): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackCutoffVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackCutoffVarianceEngine(flags: Registry): FallbackCutoffVarianceEngine {
    return FallbackCutoffVarianceEngine(
        steps = mutableListOf<FallbackCutoffVarianceStep>(
            CapFallbackCutoffVarianceAdjuster(4713.0),
            PercentFallbackCutoffVarianceAdjuster(-1.0),
            FlaggedFallbackCutoffVarianceAdjuster(flags, "fallback-cutoff-variance-primary", 8.0),
            TieredFallbackCutoffVarianceAdjuster(
                mutableListOf<Double>(1732.0, 3837.0, 5297.0),
                mutableListOf<Double>(1.18, 0.83, 1.11),
            ),
            MinimumFallbackCutoffVarianceAdjuster(36.0),
            RoundingFallbackCutoffVarianceAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 74030.8,
    )
}
