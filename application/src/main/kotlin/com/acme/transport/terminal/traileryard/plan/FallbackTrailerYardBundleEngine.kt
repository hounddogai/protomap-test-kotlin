package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback trailer yard bundle calculation. */
interface FallbackTrailerYardBundleStep {
    fun describe(): String
    fun adjust(item: FallbackTrailerYardBundle, value: Double): Double
}

data class CapFallbackTrailerYardBundleAdjuster(var cap: Double) : FallbackTrailerYardBundleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackTrailerYardBundle, value: Double): Double = minOf(value, cap)
}

data class FallbackTrailerYardBundleEndsAtOffsetAdjuster(var weight: Double) : FallbackTrailerYardBundleStep {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: FallbackTrailerYardBundle, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

data class RoundingFallbackTrailerYardBundleAdjuster(var step: Double) : FallbackTrailerYardBundleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackTrailerYardBundle, value: Double): Double = roundTo(value, step)
}

data class TieredFallbackTrailerYardBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackTrailerYardBundleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackTrailerYardBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFallbackTrailerYardBundleAdjuster(var percent: Double) : FallbackTrailerYardBundleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackTrailerYardBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback trailer yard bundle steps in order and keeps a trace of each. */
data class FallbackTrailerYardBundleEngine(
    var steps: MutableList<FallbackTrailerYardBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackTrailerYardBundle): Double {
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

    fun evaluateAll(items: MutableList<FallbackTrailerYardBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackTrailerYardBundleEngine(): FallbackTrailerYardBundleEngine {
    return FallbackTrailerYardBundleEngine(
        steps = mutableListOf<FallbackTrailerYardBundleStep>(
            CapFallbackTrailerYardBundleAdjuster(14061.0),
            FallbackTrailerYardBundleEndsAtOffsetAdjuster(1.98),
            RoundingFallbackTrailerYardBundleAdjuster(0.25),
            TieredFallbackTrailerYardBundleAdjuster(
                mutableListOf<Double>(61.0, 4411.0, 6858.0),
                mutableListOf<Double>(0.95, 0.97, 1.03),
            ),
            PercentFallbackTrailerYardBundleAdjuster(-6.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 12120.8,
    )
}
