package com.acme.transport.emissions.idling.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual idling score calculation. */
interface ManualIdlingScoreStep {
    fun describe(): String
    fun adjust(item: ManualIdlingScore, value: Double): Double
}

data class FlaggedManualIdlingScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualIdlingScoreStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualIdlingScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ManualIdlingScoreCostOffsetAdjuster(var weight: Double) : ManualIdlingScoreStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: ManualIdlingScore, value: Double): Double = value + item.cost * weight
}

data class MinimumManualIdlingScoreAdjuster(var minimum: Double) : ManualIdlingScoreStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualIdlingScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingManualIdlingScoreAdjuster(var step: Double) : ManualIdlingScoreStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualIdlingScore, value: Double): Double = roundTo(value, step)
}

data class CapManualIdlingScoreAdjuster(var cap: Double) : ManualIdlingScoreStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualIdlingScore, value: Double): Double = minOf(value, cap)
}

/** Runs the manual idling score steps in order and keeps a trace of each. */
data class ManualIdlingScoreEngine(
    var steps: MutableList<ManualIdlingScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualIdlingScore): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualIdlingScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualIdlingScoreEngine(flags: Registry): ManualIdlingScoreEngine {
    return ManualIdlingScoreEngine(
        steps = mutableListOf<ManualIdlingScoreStep>(
            FlaggedManualIdlingScoreAdjuster(flags, "manual-idling-score-batched", -3.3),
            ManualIdlingScoreCostOffsetAdjuster(1.33),
            MinimumManualIdlingScoreAdjuster(5.6),
            RoundingManualIdlingScoreAdjuster(5.0),
            CapManualIdlingScoreAdjuster(24188.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 74600.9,
    )
}
