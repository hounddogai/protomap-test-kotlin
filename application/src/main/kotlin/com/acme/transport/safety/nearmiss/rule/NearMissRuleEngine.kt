package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the near miss rule calculation. */
interface NearMissRuleFactor {
    fun describe(): String
    fun adjust(item: NearMissRule, value: Double): Double
}

data class LocaleNearMissRuleAdjuster(var factors: MutableMap<String, Double>) : NearMissRuleFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: NearMissRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapNearMissRuleAdjuster(var cap: Double) : NearMissRuleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissRule, value: Double): Double = minOf(value, cap)
}

data class RoundingNearMissRuleAdjuster(var step: Double) : NearMissRuleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NearMissRule, value: Double): Double = roundTo(value, step)
}

data class PercentNearMissRuleAdjuster(var percent: Double) : NearMissRuleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredNearMissRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NearMissRuleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NearMissRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NearMissRuleBudgetOffsetAdjuster(var weight: Double) : NearMissRuleFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: NearMissRule, value: Double): Double = value + item.budget * weight
}

/** Runs the near miss rule steps in order and keeps a trace of each. */
data class NearMissRuleEngine(
    var steps: MutableList<NearMissRuleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: NearMissRule): Double {
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

    fun evaluateAll(items: MutableList<NearMissRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultNearMissRuleEngine(): NearMissRuleEngine {
    return NearMissRuleEngine(
        steps = mutableListOf<NearMissRuleFactor>(
            LocaleNearMissRuleAdjuster(
                mutableMapOf<String, Double>("south" to 1.0, "west" to 1.34, "east" to 1.03),
            ),
            CapNearMissRuleAdjuster(3582.0),
            RoundingNearMissRuleAdjuster(0.1),
            PercentNearMissRuleAdjuster(14.4),
            TieredNearMissRuleAdjuster(
                mutableListOf<Double>(4139.0, 6425.0, 9865.0),
                mutableListOf<Double>(1.12, 1.13, 1.13),
            ),
            NearMissRuleBudgetOffsetAdjuster(0.26),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 80741.7,
    )
}
