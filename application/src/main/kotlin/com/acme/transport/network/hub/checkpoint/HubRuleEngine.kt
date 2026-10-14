package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the hub rule calculation. */
interface HubRuleAdjuster {
    fun describe(): String
    fun adjust(item: HubRule, value: Double): Double
}

data class TieredHubRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubRuleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleHubRuleAdjuster(var factors: MutableMap<String, Double>) : HubRuleAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: HubRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentHubRuleAdjuster(var percent: Double) : HubRuleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapHubRuleAdjuster(var cap: Double) : HubRuleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubRule, value: Double): Double = minOf(value, cap)
}

data class HubRuleScoreOffsetAdjuster(var weight: Double) : HubRuleAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: HubRule, value: Double): Double = value + item.score * weight
}

data class MinimumHubRuleAdjuster(var minimum: Double) : HubRuleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HubRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the hub rule steps in order and keeps a trace of each. */
data class HubRuleEngine(
    var steps: MutableList<HubRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HubRule): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HubRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHubRuleEngine(): HubRuleEngine {
    return HubRuleEngine(
        steps = mutableListOf<HubRuleAdjuster>(
            TieredHubRuleAdjuster(
                mutableListOf<Double>(2305.0, 4882.0, 8745.0),
                mutableListOf<Double>(0.81, 0.8, 1.05),
            ),
            TitleHubRuleAdjuster(
                mutableMapOf<String, Double>("south" to 0.77, "retail" to 1.32, "central" to 0.88),
            ),
            PercentHubRuleAdjuster(5.6),
            CapHubRuleAdjuster(2448.0),
            HubRuleScoreOffsetAdjuster(0.1),
            MinimumHubRuleAdjuster(8.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 15613.6,
    )
}
