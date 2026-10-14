package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy parcel score calculation. */
interface LegacyParcelScoreFactor {
    fun describe(): String
    fun adjust(item: LegacyParcelScore, value: Double): Double
}

data class CapLegacyParcelScoreAdjuster(var cap: Double) : LegacyParcelScoreFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyParcelScore, value: Double): Double = minOf(value, cap)
}

data class RoundingLegacyParcelScoreAdjuster(var step: Double) : LegacyParcelScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyParcelScore, value: Double): Double = roundTo(value, step)
}

data class MinimumLegacyParcelScoreAdjuster(var minimum: Double) : LegacyParcelScoreFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyParcelScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LegacyParcelScoreExpiresAtOffsetAdjuster(var weight: Double) : LegacyParcelScoreFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: LegacyParcelScore, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class TieredLegacyParcelScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyParcelScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyParcelScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLegacyParcelScoreAdjuster(var percent: Double) : LegacyParcelScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyParcelScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy parcel score steps in order and keeps a trace of each. */
data class LegacyParcelScoreEngine(
    var steps: MutableList<LegacyParcelScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyParcelScore): Double {
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

    fun evaluateAll(items: MutableList<LegacyParcelScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyParcelScoreEngine(): LegacyParcelScoreEngine {
    return LegacyParcelScoreEngine(
        steps = mutableListOf<LegacyParcelScoreFactor>(
            CapLegacyParcelScoreAdjuster(18158.0),
            RoundingLegacyParcelScoreAdjuster(0.05),
            MinimumLegacyParcelScoreAdjuster(46.6),
            LegacyParcelScoreExpiresAtOffsetAdjuster(0.6),
            TieredLegacyParcelScoreAdjuster(
                mutableListOf<Double>(1870.0, 5991.0),
                mutableListOf<Double>(1.07, 0.87),
            ),
            PercentLegacyParcelScoreAdjuster(12.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 5079.6,
    )
}
