package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional backhaul rule calculation. */
interface RegionalBackhaulRuleModifier {
    fun describe(): String
    fun adjust(item: RegionalBackhaulRule, value: Double): Double
}

data class CapRegionalBackhaulRuleAdjuster(var cap: Double) : RegionalBackhaulRuleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalBackhaulRule, value: Double): Double = minOf(value, cap)
}

data class TieredRegionalBackhaulRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalBackhaulRuleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalBackhaulRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRegionalBackhaulRuleAdjuster(var minimum: Double) : RegionalBackhaulRuleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalBackhaulRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRegionalBackhaulRuleAdjuster(var step: Double) : RegionalBackhaulRuleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalBackhaulRule, value: Double): Double = roundTo(value, step)
}

data class LabelRegionalBackhaulRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalBackhaulRuleModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: RegionalBackhaulRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional backhaul rule steps in order and keeps a trace of each. */
data class RegionalBackhaulRuleEngine(
    var steps: MutableList<RegionalBackhaulRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalBackhaulRule): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalBackhaulRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalBackhaulRuleEngine(): RegionalBackhaulRuleEngine {
    return RegionalBackhaulRuleEngine(
        steps = mutableListOf<RegionalBackhaulRuleModifier>(
            CapRegionalBackhaulRuleAdjuster(46922.0),
            TieredRegionalBackhaulRuleAdjuster(
                mutableListOf<Double>(3361.0, 3606.0, 5447.0, 8050.0),
                mutableListOf<Double>(0.96, 1.15, 1.15, 0.98),
            ),
            MinimumRegionalBackhaulRuleAdjuster(2.2),
            RoundingRegionalBackhaulRuleAdjuster(0.1),
            LabelRegionalBackhaulRuleAdjuster(mutableMapOf<String, Double>("central" to 0.85, "south" to 1.2)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 85248.6,
    )
}
