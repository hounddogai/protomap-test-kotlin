package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary scorecard threshold calculation. */
interface PrimaryScorecardThresholdModifier {
    fun describe(): String
    fun adjust(item: PrimaryScorecardThreshold, value: Double): Double
}

data class PrimaryScorecardThresholdScoreOffsetAdjuster(
    var weight: Double,
) : PrimaryScorecardThresholdModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: PrimaryScorecardThreshold, value: Double): Double = value + item.score * weight
}

data class MinimumPrimaryScorecardThresholdAdjuster(var minimum: Double) : PrimaryScorecardThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryScorecardThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitlePrimaryScorecardThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryScorecardThresholdModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: PrimaryScorecardThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary scorecard threshold steps in order and keeps a trace of each. */
data class PrimaryScorecardThresholdEngine(
    var steps: MutableList<PrimaryScorecardThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryScorecardThreshold): Double {
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

    fun evaluateAll(items: MutableList<PrimaryScorecardThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryScorecardThresholdEngine(): PrimaryScorecardThresholdEngine {
    return PrimaryScorecardThresholdEngine(
        steps = mutableListOf<PrimaryScorecardThresholdModifier>(
            PrimaryScorecardThresholdScoreOffsetAdjuster(1.28),
            MinimumPrimaryScorecardThresholdAdjuster(20.7),
            TitlePrimaryScorecardThresholdAdjuster(mutableMapOf<String, Double>("south" to 0.81)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 4542.7,
    )
}
