package com.acme.transport.linehaul.laneplan.policy

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lane plan rule calculation. */
interface LanePlanRuleAdjuster {
    fun describe(): String
    fun adjust(item: LanePlanRule, value: Double): Double
}

data class MinimumLanePlanRuleAdjuster(var minimum: Double) : LanePlanRuleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LanePlanRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LanePlanRuleFuelLitersOffsetAdjuster(var weight: Double) : LanePlanRuleAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: LanePlanRule, value: Double): Double = value + item.fuelLiters * weight
}

data class LaneLanePlanRuleAdjuster(var factors: MutableMap<String, Double>) : LanePlanRuleAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: LanePlanRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLanePlanRuleAdjuster(var percent: Double) : LanePlanRuleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LanePlanRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLanePlanRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanRuleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLanePlanRuleAdjuster(var cap: Double) : LanePlanRuleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanRule, value: Double): Double = minOf(value, cap)
}

/** Runs the lane plan rule steps in order and keeps a trace of each. */
data class LanePlanRuleEngine(
    var steps: MutableList<LanePlanRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LanePlanRule): Double {
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

    fun evaluateAll(items: MutableList<LanePlanRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLanePlanRuleEngine(): LanePlanRuleEngine {
    return LanePlanRuleEngine(
        steps = mutableListOf<LanePlanRuleAdjuster>(
            MinimumLanePlanRuleAdjuster(12.1),
            LanePlanRuleFuelLitersOffsetAdjuster(0.8),
            LaneLanePlanRuleAdjuster(mutableMapOf<String, Double>("central" to 1.32)),
            PercentLanePlanRuleAdjuster(-17.3),
            TieredLanePlanRuleAdjuster(mutableListOf<Double>(1508.0, 4062.0), mutableListOf<Double>(0.86, 0.99)),
            CapLanePlanRuleAdjuster(13088.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 45590.2,
    )
}
