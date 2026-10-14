package com.acme.transport.fleetops.inspection.inspectionslot

import com.acme.transport.kit.flags.Registry

/** One step of the inspection rule calculation. */
interface InspectionRuleFactor {
    fun describe(): String
    fun adjust(item: InspectionRule, value: Double): Double
}

data class LabelInspectionRuleAdjuster(var factors: MutableMap<String, Double>) : InspectionRuleFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: InspectionRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredInspectionRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionRuleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumInspectionRuleAdjuster(var minimum: Double) : InspectionRuleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapInspectionRuleAdjuster(var cap: Double) : InspectionRuleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionRule, value: Double): Double = minOf(value, cap)
}

data class FlaggedInspectionRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionRuleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the inspection rule steps in order and keeps a trace of each. */
data class InspectionRuleEngine(
    var steps: MutableList<InspectionRuleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: InspectionRule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionRuleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultInspectionRuleEngine(flags: Registry): InspectionRuleEngine {
    return InspectionRuleEngine(
        steps = mutableListOf<InspectionRuleFactor>(
            LabelInspectionRuleAdjuster(mutableMapOf<String, Double>("north" to 1.2)),
            TieredInspectionRuleAdjuster(
                mutableListOf<Double>(470.0, 4109.0, 5963.0, 6135.0),
                mutableListOf<Double>(0.96, 0.92, 0.92, 1.1),
            ),
            MinimumInspectionRuleAdjuster(3.6),
            CapInspectionRuleAdjuster(34015.0),
            FlaggedInspectionRuleAdjuster(flags, "inspection-rule-default", 5.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 15271.8,
    )
}
