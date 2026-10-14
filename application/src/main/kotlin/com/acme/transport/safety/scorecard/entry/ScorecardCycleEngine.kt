package com.acme.transport.safety.scorecard.entry

import com.acme.transport.kit.numbers.roundTo

/** One step of the scorecard cycle calculation. */
interface ScorecardCycleStep {
    fun describe(): String
    fun adjust(item: ScorecardCycle, value: Double): Double
}

data class LocaleScorecardCycleAdjuster(var factors: MutableMap<String, Double>) : ScorecardCycleStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: ScorecardCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScorecardCycleAdjuster(var cap: Double) : ScorecardCycleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScorecardCycle, value: Double): Double = minOf(value, cap)
}

data class ScorecardCycleMinimumOffsetAdjuster(var weight: Double) : ScorecardCycleStep {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: ScorecardCycle, value: Double): Double = value + item.minimum * weight
}

data class RoundingScorecardCycleAdjuster(var step: Double) : ScorecardCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardCycle, value: Double): Double = roundTo(value, step)
}

data class TieredScorecardCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScorecardCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScorecardCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scorecard cycle steps in order and keeps a trace of each. */
data class ScorecardCycleEngine(
    var steps: MutableList<ScorecardCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScorecardCycle): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardCycleStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScorecardCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScorecardCycleEngine(): ScorecardCycleEngine {
    return ScorecardCycleEngine(
        steps = mutableListOf<ScorecardCycleStep>(
            LocaleScorecardCycleAdjuster(mutableMapOf<String, Double>("east" to 0.88, "central" to 0.8)),
            CapScorecardCycleAdjuster(17174.0),
            ScorecardCycleMinimumOffsetAdjuster(1.59),
            RoundingScorecardCycleAdjuster(0.5),
            TieredScorecardCycleAdjuster(
                mutableListOf<Double>(1120.0, 3278.0, 4331.0, 6580.0),
                mutableListOf<Double>(1.01, 1.0, 0.85, 1.07),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 71247.2,
    )
}
