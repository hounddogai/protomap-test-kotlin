package com.acme.transport.linehaul.trailer.bundle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy trailer variance calculation. */
interface LegacyTrailerVarianceModifier {
    fun describe(): String
    fun adjust(item: LegacyTrailerVariance, value: Double): Double
}

data class MinimumLegacyTrailerVarianceAdjuster(var minimum: Double) : LegacyTrailerVarianceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyTrailerVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelLegacyTrailerVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyTrailerVarianceModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: LegacyTrailerVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLegacyTrailerVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyTrailerVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyTrailerVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLegacyTrailerVarianceAdjuster(var percent: Double) : LegacyTrailerVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyTrailerVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy trailer variance steps in order and keeps a trace of each. */
data class LegacyTrailerVarianceEngine(
    var steps: MutableList<LegacyTrailerVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyTrailerVariance): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyTrailerVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyTrailerVarianceEngine(): LegacyTrailerVarianceEngine {
    return LegacyTrailerVarianceEngine(
        steps = mutableListOf<LegacyTrailerVarianceModifier>(
            MinimumLegacyTrailerVarianceAdjuster(3.5),
            LabelLegacyTrailerVarianceAdjuster(
                mutableMapOf<String, Double>("central" to 0.94, "north" to 1.27, "retail" to 0.77),
            ),
            TieredLegacyTrailerVarianceAdjuster(
                mutableListOf<Double>(1755.0, 4117.0, 8046.0),
                mutableListOf<Double>(0.96, 1.06, 0.97),
            ),
            PercentLegacyTrailerVarianceAdjuster(10.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 49464.5,
    )
}
