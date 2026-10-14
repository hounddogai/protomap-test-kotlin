package com.acme.transport.emissions.evcharge.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional ev charge score calculation. */
interface RegionalEvChargeScoreFactor {
    fun describe(): String
    fun adjust(item: RegionalEvChargeScore, value: Double): Double
}

data class RegionalEvChargeScoreRevisionOffsetAdjuster(var weight: Double) : RegionalEvChargeScoreFactor {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: RegionalEvChargeScore, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class RoundingRegionalEvChargeScoreAdjuster(var step: Double) : RegionalEvChargeScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalEvChargeScore, value: Double): Double = roundTo(value, step)
}

data class FlaggedRegionalEvChargeScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalEvChargeScoreFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalEvChargeScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional ev charge score steps in order and keeps a trace of each. */
data class RegionalEvChargeScoreEngine(
    var steps: MutableList<RegionalEvChargeScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalEvChargeScore): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalEvChargeScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalEvChargeScoreEngine(flags: Registry): RegionalEvChargeScoreEngine {
    return RegionalEvChargeScoreEngine(
        steps = mutableListOf<RegionalEvChargeScoreFactor>(
            RegionalEvChargeScoreRevisionOffsetAdjuster(0.04),
            RoundingRegionalEvChargeScoreAdjuster(1.0),
            FlaggedRegionalEvChargeScoreAdjuster(flags, "regional-ev-charge-score-tiered", -6.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 74779.0,
    )
}
