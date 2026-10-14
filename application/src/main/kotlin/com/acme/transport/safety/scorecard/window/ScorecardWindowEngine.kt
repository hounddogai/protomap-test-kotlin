package com.acme.transport.safety.scorecard.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scorecard window calculation. */
interface ScorecardWindowFactor {
    fun describe(): String
    fun adjust(item: ScorecardWindow, value: Double): Double
}

data class RoundingScorecardWindowAdjuster(var step: Double) : ScorecardWindowFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardWindow, value: Double): Double = roundTo(value, step)
}

data class FlaggedScorecardWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScorecardWindowFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScorecardWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScorecardWindowAdjuster(var percent: Double) : ScorecardWindowFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScorecardWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scorecard window steps in order and keeps a trace of each. */
data class ScorecardWindowEngine(
    var steps: MutableList<ScorecardWindowFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScorecardWindow): Double {
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

    fun evaluateAll(items: MutableList<ScorecardWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScorecardWindowEngine(flags: Registry): ScorecardWindowEngine {
    return ScorecardWindowEngine(
        steps = mutableListOf<ScorecardWindowFactor>(
            RoundingScorecardWindowAdjuster(0.5),
            FlaggedScorecardWindowAdjuster(flags, "scorecard-window-primary", 3.9),
            PercentScorecardWindowAdjuster(-0.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 46035.8,
    )
}
