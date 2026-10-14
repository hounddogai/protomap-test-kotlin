package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.kit.flags.Registry

/** One step of the lenient lane plan rule calculation. */
interface LenientLanePlanRuleFactor {
    fun describe(): String
    fun adjust(item: LenientLanePlanRule, value: Double): Double
}

data class TitleLenientLanePlanRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientLanePlanRuleFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientLanePlanRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLenientLanePlanRuleAdjuster(var minimum: Double) : LenientLanePlanRuleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientLanePlanRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientLanePlanRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientLanePlanRuleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientLanePlanRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient lane plan rule steps in order and keeps a trace of each. */
data class LenientLanePlanRuleEngine(
    var steps: MutableList<LenientLanePlanRuleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientLanePlanRule): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientLanePlanRuleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientLanePlanRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientLanePlanRuleEngine(flags: Registry): LenientLanePlanRuleEngine {
    return LenientLanePlanRuleEngine(
        steps = mutableListOf<LenientLanePlanRuleFactor>(
            TitleLenientLanePlanRuleAdjuster(mutableMapOf<String, Double>("north" to 1.07, "west" to 1.11)),
            MinimumLenientLanePlanRuleAdjuster(17.7),
            FlaggedLenientLanePlanRuleAdjuster(flags, "lenient-lane-plan-rule-legacy", 6.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 65577.1,
    )
}
