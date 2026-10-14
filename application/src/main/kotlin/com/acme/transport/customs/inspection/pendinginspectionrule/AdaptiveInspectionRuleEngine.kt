package com.acme.transport.customs.inspection.pendinginspectionrule

/** One step of the adaptive inspection rule calculation. */
interface AdaptiveInspectionRuleModifier {
    fun describe(): String
    fun adjust(item: AdaptiveInspectionRule, value: Double): Double
}

data class TieredAdaptiveInspectionRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveInspectionRuleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveInspectionRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AdaptiveInspectionRuleWindowDaysOffsetAdjuster(var weight: Double) : AdaptiveInspectionRuleModifier {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: AdaptiveInspectionRule, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class LabelAdaptiveInspectionRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveInspectionRuleModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: AdaptiveInspectionRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAdaptiveInspectionRuleAdjuster(var minimum: Double) : AdaptiveInspectionRuleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveInspectionRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAdaptiveInspectionRuleAdjuster(var cap: Double) : AdaptiveInspectionRuleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveInspectionRule, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive inspection rule steps in order and keeps a trace of each. */
data class AdaptiveInspectionRuleEngine(
    var steps: MutableList<AdaptiveInspectionRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveInspectionRule): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveInspectionRuleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveInspectionRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveInspectionRuleEngine(): AdaptiveInspectionRuleEngine {
    return AdaptiveInspectionRuleEngine(
        steps = mutableListOf<AdaptiveInspectionRuleModifier>(
            TieredAdaptiveInspectionRuleAdjuster(
                mutableListOf<Double>(3515.0, 5304.0, 8813.0, 9029.0),
                mutableListOf<Double>(1.01, 0.89, 0.85, 1.18),
            ),
            AdaptiveInspectionRuleWindowDaysOffsetAdjuster(0.32),
            LabelAdaptiveInspectionRuleAdjuster(
                mutableMapOf<String, Double>("west" to 0.78, "north" to 1.37, "south" to 1.04),
            ),
            MinimumAdaptiveInspectionRuleAdjuster(4.5),
            CapAdaptiveInspectionRuleAdjuster(14664.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 48528.8,
    )
}
