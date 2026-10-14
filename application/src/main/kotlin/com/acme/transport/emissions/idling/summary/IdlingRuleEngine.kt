package com.acme.transport.emissions.idling.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the idling rule calculation. */
interface IdlingRuleStep {
    fun describe(): String
    fun adjust(item: IdlingRule, value: Double): Double
}

data class FlaggedIdlingRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingRuleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingIdlingRuleAdjuster(var step: Double) : IdlingRuleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IdlingRule, value: Double): Double = roundTo(value, step)
}

data class MinimumIdlingRuleAdjuster(var minimum: Double) : IdlingRuleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredIdlingRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingRuleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotIdlingRuleAdjuster(var factors: MutableMap<String, Double>) : IdlingRuleStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: IdlingRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapIdlingRuleAdjuster(var cap: Double) : IdlingRuleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingRule, value: Double): Double = minOf(value, cap)
}

/** Runs the idling rule steps in order and keeps a trace of each. */
data class IdlingRuleEngine(
    var steps: MutableList<IdlingRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: IdlingRule): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IdlingRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultIdlingRuleEngine(flags: Registry): IdlingRuleEngine {
    return IdlingRuleEngine(
        steps = mutableListOf<IdlingRuleStep>(
            FlaggedIdlingRuleAdjuster(flags, "idling-rule-strict", 8.4),
            RoundingIdlingRuleAdjuster(0.1),
            MinimumIdlingRuleAdjuster(4.3),
            TieredIdlingRuleAdjuster(
                mutableListOf<Double>(3107.0, 4808.0, 8676.0),
                mutableListOf<Double>(1.06, 1.09, 0.99),
            ),
            DepotIdlingRuleAdjuster(mutableMapOf<String, Double>("online" to 1.24, "central" to 1.19)),
            CapIdlingRuleAdjuster(7788.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 82972.4,
    )
}
