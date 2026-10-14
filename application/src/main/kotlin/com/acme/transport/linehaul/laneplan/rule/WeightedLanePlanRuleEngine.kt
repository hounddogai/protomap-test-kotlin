package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted lane plan rule calculation. */
interface WeightedLanePlanRuleAdjuster {
    fun describe(): String
    fun adjust(item: WeightedLanePlanRule, value: Double): Double
}

data class CapWeightedLanePlanRuleAdjuster(var cap: Double) : WeightedLanePlanRuleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedLanePlanRule, value: Double): Double = minOf(value, cap)
}

data class PercentWeightedLanePlanRuleAdjuster(var percent: Double) : WeightedLanePlanRuleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedLanePlanRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedWeightedLanePlanRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedLanePlanRuleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedLanePlanRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted lane plan rule steps in order and keeps a trace of each. */
data class WeightedLanePlanRuleEngine(
    var steps: MutableList<WeightedLanePlanRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedLanePlanRule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedLanePlanRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedLanePlanRuleEngine(flags: Registry): WeightedLanePlanRuleEngine {
    return WeightedLanePlanRuleEngine(
        steps = mutableListOf<WeightedLanePlanRuleAdjuster>(
            CapWeightedLanePlanRuleAdjuster(33154.0),
            PercentWeightedLanePlanRuleAdjuster(23.3),
            FlaggedWeightedLanePlanRuleAdjuster(flags, "weighted-lane-plan-rule-capped", 4.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 16611.4,
    )
}
