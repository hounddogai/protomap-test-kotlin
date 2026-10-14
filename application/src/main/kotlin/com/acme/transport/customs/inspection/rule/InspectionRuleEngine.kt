package com.acme.transport.customs.inspection.rule

/** One step of the inspection rule calculation. */
interface InspectionRuleAdjuster {
    fun describe(): String
    fun adjust(item: InspectionRule, value: Double): Double
}

data class PercentInspectionRuleAdjuster(var percent: Double) : InspectionRuleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapInspectionRuleAdjuster(var cap: Double) : InspectionRuleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionRule, value: Double): Double = minOf(value, cap)
}

data class RegionInspectionRuleAdjuster(var factors: MutableMap<String, Double>) : InspectionRuleAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: InspectionRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredInspectionRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionRuleAdjuster {
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

/** Runs the inspection rule steps in order and keeps a trace of each. */
data class InspectionRuleEngine(
    var steps: MutableList<InspectionRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: InspectionRule): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionRuleAdjuster = steps[index]
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

fun defaultInspectionRuleEngine(): InspectionRuleEngine {
    return InspectionRuleEngine(
        steps = mutableListOf<InspectionRuleAdjuster>(
            PercentInspectionRuleAdjuster(-19.2),
            CapInspectionRuleAdjuster(16793.0),
            RegionInspectionRuleAdjuster(
                mutableMapOf<String, Double>("central" to 1.35, "north" to 0.78, "west" to 1.15),
            ),
            TieredInspectionRuleAdjuster(
                mutableListOf<Double>(3764.0, 4623.0, 5828.0),
                mutableListOf<Double>(1.16, 0.84, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 65121.9,
    )
}
