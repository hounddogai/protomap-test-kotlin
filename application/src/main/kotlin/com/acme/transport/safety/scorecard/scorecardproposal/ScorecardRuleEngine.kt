package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scorecard rule calculation. */
interface ScorecardRuleModifier {
    fun describe(): String
    fun adjust(item: ScorecardRule, value: Double): Double
}

data class SourceScorecardRuleAdjuster(var factors: MutableMap<String, Double>) : ScorecardRuleModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: ScorecardRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScorecardRuleAdjuster(var cap: Double) : ScorecardRuleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScorecardRule, value: Double): Double = minOf(value, cap)
}

data class TieredScorecardRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScorecardRuleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScorecardRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ScorecardRuleMinimumOffsetAdjuster(var weight: Double) : ScorecardRuleModifier {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: ScorecardRule, value: Double): Double = value + item.minimum * weight
}

/** Runs the scorecard rule steps in order and keeps a trace of each. */
data class ScorecardRuleEngine(
    var steps: MutableList<ScorecardRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScorecardRule): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScorecardRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScorecardRuleEngine(): ScorecardRuleEngine {
    return ScorecardRuleEngine(
        steps = mutableListOf<ScorecardRuleModifier>(
            SourceScorecardRuleAdjuster(mutableMapOf<String, Double>("retail" to 0.81)),
            CapScorecardRuleAdjuster(45142.0),
            TieredScorecardRuleAdjuster(
                mutableListOf<Double>(810.0, 7573.0, 8859.0),
                mutableListOf<Double>(0.96, 1.02, 0.84),
            ),
            ScorecardRuleMinimumOffsetAdjuster(1.21),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 77452.1,
    )
}
