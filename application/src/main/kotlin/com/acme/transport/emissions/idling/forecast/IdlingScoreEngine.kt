package com.acme.transport.emissions.idling.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the idling score calculation. */
interface IdlingScoreFactor {
    fun describe(): String
    fun adjust(item: IdlingScore, value: Double): Double
}

data class IdlingScoreDistanceKmOffsetAdjuster(var weight: Double) : IdlingScoreFactor {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: IdlingScore, value: Double): Double = value + item.distanceKm * weight
}

data class LabelIdlingScoreAdjuster(var factors: MutableMap<String, Double>) : IdlingScoreFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: IdlingScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentIdlingScoreAdjuster(var percent: Double) : IdlingScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingIdlingScoreAdjuster(var step: Double) : IdlingScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IdlingScore, value: Double): Double = roundTo(value, step)
}

data class FlaggedIdlingScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingScoreFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the idling score steps in order and keeps a trace of each. */
data class IdlingScoreEngine(
    var steps: MutableList<IdlingScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: IdlingScore): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IdlingScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultIdlingScoreEngine(flags: Registry): IdlingScoreEngine {
    return IdlingScoreEngine(
        steps = mutableListOf<IdlingScoreFactor>(
            IdlingScoreDistanceKmOffsetAdjuster(1.42),
            LabelIdlingScoreAdjuster(mutableMapOf<String, Double>("central" to 1.29)),
            PercentIdlingScoreAdjuster(12.9),
            RoundingIdlingScoreAdjuster(0.05),
            FlaggedIdlingScoreAdjuster(flags, "idling-score-rolling", 6.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 78195.5,
    )
}
