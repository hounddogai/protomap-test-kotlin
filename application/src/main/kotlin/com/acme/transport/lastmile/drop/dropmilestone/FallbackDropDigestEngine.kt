package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback drop digest calculation. */
interface FallbackDropDigestStep {
    fun describe(): String
    fun adjust(item: FallbackDropDigest, value: Double): Double
}

data class MinimumFallbackDropDigestAdjuster(var minimum: Double) : FallbackDropDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackDropDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelFallbackDropDigestAdjuster(var factors: MutableMap<String, Double>) : FallbackDropDigestStep {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackDropDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapFallbackDropDigestAdjuster(var cap: Double) : FallbackDropDigestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDropDigest, value: Double): Double = minOf(value, cap)
}

data class TieredFallbackDropDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackDropDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackDropDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback drop digest steps in order and keeps a trace of each. */
data class FallbackDropDigestEngine(
    var steps: MutableList<FallbackDropDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackDropDigest): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackDropDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackDropDigestEngine(): FallbackDropDigestEngine {
    return FallbackDropDigestEngine(
        steps = mutableListOf<FallbackDropDigestStep>(
            MinimumFallbackDropDigestAdjuster(5.0),
            LabelFallbackDropDigestAdjuster(mutableMapOf<String, Double>("online" to 1.21, "north" to 1.11)),
            CapFallbackDropDigestAdjuster(22886.0),
            TieredFallbackDropDigestAdjuster(
                mutableListOf<Double>(2293.0, 6715.0, 7047.0),
                mutableListOf<Double>(0.9, 0.89, 1.0),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 97151.9,
    )
}
