package com.acme.transport.linehaul.trailer.claim

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient trailer variance calculation. */
interface LenientTrailerVarianceModifier {
    fun describe(): String
    fun adjust(item: LenientTrailerVariance, value: Double): Double
}

data class NameLenientTrailerVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientTrailerVarianceModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: LenientTrailerVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLenientTrailerVarianceAdjuster(var percent: Double) : LenientTrailerVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientTrailerVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LenientTrailerVarianceLimitOffsetAdjuster(var weight: Double) : LenientTrailerVarianceModifier {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: LenientTrailerVariance, value: Double): Double = value + item.limit.toDouble() * weight
}

data class MinimumLenientTrailerVarianceAdjuster(var minimum: Double) : LenientTrailerVarianceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientTrailerVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient trailer variance steps in order and keeps a trace of each. */
data class LenientTrailerVarianceEngine(
    var steps: MutableList<LenientTrailerVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientTrailerVariance): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientTrailerVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientTrailerVarianceEngine(): LenientTrailerVarianceEngine {
    return LenientTrailerVarianceEngine(
        steps = mutableListOf<LenientTrailerVarianceModifier>(
            NameLenientTrailerVarianceAdjuster(
                mutableMapOf<String, Double>("central" to 1.38, "online" to 1.21),
            ),
            PercentLenientTrailerVarianceAdjuster(-10.6),
            LenientTrailerVarianceLimitOffsetAdjuster(1.55),
            MinimumLenientTrailerVarianceAdjuster(48.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 79229.8,
    )
}
