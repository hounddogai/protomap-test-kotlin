package com.acme.transport.linehaul.laneplan.schedule

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback lane plan rule calculation. */
interface FallbackLanePlanRuleAdjuster {
    fun describe(): String
    fun adjust(item: FallbackLanePlanRule, value: Double): Double
}

data class MinimumFallbackLanePlanRuleAdjuster(var minimum: Double) : FallbackLanePlanRuleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackLanePlanRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFallbackLanePlanRuleAdjuster(var cap: Double) : FallbackLanePlanRuleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackLanePlanRule, value: Double): Double = minOf(value, cap)
}

data class LabelFallbackLanePlanRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackLanePlanRuleAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackLanePlanRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingFallbackLanePlanRuleAdjuster(var step: Double) : FallbackLanePlanRuleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackLanePlanRule, value: Double): Double = roundTo(value, step)
}

data class TieredFallbackLanePlanRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackLanePlanRuleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackLanePlanRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FallbackLanePlanRuleThresholdOffsetAdjuster(var weight: Double) : FallbackLanePlanRuleAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: FallbackLanePlanRule, value: Double): Double = value + item.threshold * weight
}

/** Runs the fallback lane plan rule steps in order and keeps a trace of each. */
data class FallbackLanePlanRuleEngine(
    var steps: MutableList<FallbackLanePlanRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackLanePlanRule): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackLanePlanRuleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackLanePlanRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackLanePlanRuleEngine(): FallbackLanePlanRuleEngine {
    return FallbackLanePlanRuleEngine(
        steps = mutableListOf<FallbackLanePlanRuleAdjuster>(
            MinimumFallbackLanePlanRuleAdjuster(46.7),
            CapFallbackLanePlanRuleAdjuster(49718.0),
            LabelFallbackLanePlanRuleAdjuster(mutableMapOf<String, Double>("east" to 1.16, "south" to 1.07)),
            RoundingFallbackLanePlanRuleAdjuster(1.0),
            TieredFallbackLanePlanRuleAdjuster(
                mutableListOf<Double>(1016.0, 6933.0),
                mutableListOf<Double>(1.01, 1.0),
            ),
            FallbackLanePlanRuleThresholdOffsetAdjuster(1.47),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 29167.1,
    )
}
