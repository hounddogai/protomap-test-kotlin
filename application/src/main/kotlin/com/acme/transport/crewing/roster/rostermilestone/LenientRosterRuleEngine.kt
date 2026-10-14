package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient roster rule calculation. */
interface LenientRosterRuleStep {
    fun describe(): String
    fun adjust(item: LenientRosterRule, value: Double): Double
}

data class TitleLenientRosterRuleAdjuster(var factors: MutableMap<String, Double>) : LenientRosterRuleStep {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientRosterRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLenientRosterRuleAdjuster(var cap: Double) : LenientRosterRuleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientRosterRule, value: Double): Double = minOf(value, cap)
}

data class PercentLenientRosterRuleAdjuster(var percent: Double) : LenientRosterRuleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientRosterRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LenientRosterRuleMarginOffsetAdjuster(var weight: Double) : LenientRosterRuleStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: LenientRosterRule, value: Double): Double = value + item.margin * weight
}

data class RoundingLenientRosterRuleAdjuster(var step: Double) : LenientRosterRuleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientRosterRule, value: Double): Double = roundTo(value, step)
}

data class TieredLenientRosterRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientRosterRuleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientRosterRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient roster rule steps in order and keeps a trace of each. */
data class LenientRosterRuleEngine(
    var steps: MutableList<LenientRosterRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientRosterRule): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientRosterRuleStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientRosterRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientRosterRuleEngine(): LenientRosterRuleEngine {
    return LenientRosterRuleEngine(
        steps = mutableListOf<LenientRosterRuleStep>(
            TitleLenientRosterRuleAdjuster(mutableMapOf<String, Double>("central" to 1.0)),
            CapLenientRosterRuleAdjuster(34361.0),
            PercentLenientRosterRuleAdjuster(-0.3),
            LenientRosterRuleMarginOffsetAdjuster(0.11),
            RoundingLenientRosterRuleAdjuster(0.25),
            TieredLenientRosterRuleAdjuster(
                mutableListOf<Double>(2531.0, 2569.0, 3474.0),
                mutableListOf<Double>(0.92, 1.03, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 68481.7,
    )
}
