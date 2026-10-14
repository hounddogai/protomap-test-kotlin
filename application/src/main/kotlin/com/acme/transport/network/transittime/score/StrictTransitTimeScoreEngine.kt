package com.acme.transport.network.transittime.score

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict transit time score calculation. */
interface StrictTransitTimeScoreStep {
    fun describe(): String
    fun adjust(item: StrictTransitTimeScore, value: Double): Double
}

data class DepotStrictTransitTimeScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictTransitTimeScoreStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: StrictTransitTimeScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapStrictTransitTimeScoreAdjuster(var cap: Double) : StrictTransitTimeScoreStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictTransitTimeScore, value: Double): Double = minOf(value, cap)
}

data class StrictTransitTimeScoreRetriesOffsetAdjuster(var weight: Double) : StrictTransitTimeScoreStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: StrictTransitTimeScore, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class TieredStrictTransitTimeScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictTransitTimeScoreStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictTransitTimeScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingStrictTransitTimeScoreAdjuster(var step: Double) : StrictTransitTimeScoreStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictTransitTimeScore, value: Double): Double = roundTo(value, step)
}

/** Runs the strict transit time score steps in order and keeps a trace of each. */
data class StrictTransitTimeScoreEngine(
    var steps: MutableList<StrictTransitTimeScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictTransitTimeScore): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictTransitTimeScoreStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictTransitTimeScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictTransitTimeScoreEngine(): StrictTransitTimeScoreEngine {
    return StrictTransitTimeScoreEngine(
        steps = mutableListOf<StrictTransitTimeScoreStep>(
            DepotStrictTransitTimeScoreAdjuster(
                mutableMapOf<String, Double>("central" to 1.09, "retail" to 0.85),
            ),
            CapStrictTransitTimeScoreAdjuster(37395.0),
            StrictTransitTimeScoreRetriesOffsetAdjuster(0.93),
            TieredStrictTransitTimeScoreAdjuster(
                mutableListOf<Double>(3946.0, 6788.0, 7662.0, 9222.0),
                mutableListOf<Double>(0.86, 0.96, 1.2, 0.99),
            ),
            RoundingStrictTransitTimeScoreAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 44037.6,
    )
}
