package com.acme.transport.safety.scorecard.quota

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict scorecard template calculation. */
interface StrictScorecardTemplateStep {
    fun describe(): String
    fun adjust(item: StrictScorecardTemplate, value: Double): Double
}

data class TitleStrictScorecardTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictScorecardTemplateStep {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictScorecardTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapStrictScorecardTemplateAdjuster(var cap: Double) : StrictScorecardTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictScorecardTemplate, value: Double): Double = minOf(value, cap)
}

data class StrictScorecardTemplateStartsAtOffsetAdjuster(var weight: Double) : StrictScorecardTemplateStep {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: StrictScorecardTemplate, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class TieredStrictScorecardTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictScorecardTemplateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictScorecardTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictScorecardTemplateAdjuster(var minimum: Double) : StrictScorecardTemplateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictScorecardTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict scorecard template steps in order and keeps a trace of each. */
data class StrictScorecardTemplateEngine(
    var steps: MutableList<StrictScorecardTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictScorecardTemplate): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictScorecardTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictScorecardTemplateEngine(): StrictScorecardTemplateEngine {
    return StrictScorecardTemplateEngine(
        steps = mutableListOf<StrictScorecardTemplateStep>(
            TitleStrictScorecardTemplateAdjuster(
                mutableMapOf<String, Double>("east" to 1.32, "central" to 0.97, "west" to 0.85),
            ),
            CapStrictScorecardTemplateAdjuster(9631.0),
            StrictScorecardTemplateStartsAtOffsetAdjuster(1.54),
            TieredStrictScorecardTemplateAdjuster(
                mutableListOf<Double>(1087.0, 7686.0, 8886.0, 8963.0),
                mutableListOf<Double>(0.95, 1.16, 1.17, 0.85),
            ),
            MinimumStrictScorecardTemplateAdjuster(33.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 64219.2,
    )
}
