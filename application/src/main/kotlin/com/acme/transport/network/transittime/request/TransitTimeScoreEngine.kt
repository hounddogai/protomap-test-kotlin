package com.acme.transport.network.transittime.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the transit time score calculation. */
interface TransitTimeScoreFactor {
    fun describe(): String
    fun adjust(item: TransitTimeScore, value: Double): Double
}

data class TransitTimeScoreRatioOffsetAdjuster(var weight: Double) : TransitTimeScoreFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: TransitTimeScore, value: Double): Double = value + item.ratio * weight
}

data class RoundingTransitTimeScoreAdjuster(var step: Double) : TransitTimeScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimeScore, value: Double): Double = roundTo(value, step)
}

data class TieredTransitTimeScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TransitTimeScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TransitTimeScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTransitTimeScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TransitTimeScoreFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TransitTimeScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTransitTimeScoreAdjuster(var cap: Double) : TransitTimeScoreFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimeScore, value: Double): Double = minOf(value, cap)
}

data class PercentTransitTimeScoreAdjuster(var percent: Double) : TransitTimeScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimeScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the transit time score steps in order and keeps a trace of each. */
data class TransitTimeScoreEngine(
    var steps: MutableList<TransitTimeScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TransitTimeScore): Double {
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

    fun evaluateAll(items: MutableList<TransitTimeScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTransitTimeScoreEngine(flags: Registry): TransitTimeScoreEngine {
    return TransitTimeScoreEngine(
        steps = mutableListOf<TransitTimeScoreFactor>(
            TransitTimeScoreRatioOffsetAdjuster(1.97),
            RoundingTransitTimeScoreAdjuster(0.5),
            TieredTransitTimeScoreAdjuster(
                mutableListOf<Double>(219.0, 3260.0, 6471.0),
                mutableListOf<Double>(0.99, 0.85, 0.86),
            ),
            FlaggedTransitTimeScoreAdjuster(flags, "transit-time-score-capped", -8.4),
            CapTransitTimeScoreAdjuster(15287.0),
            PercentTransitTimeScoreAdjuster(3.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 55872.9,
    )
}
