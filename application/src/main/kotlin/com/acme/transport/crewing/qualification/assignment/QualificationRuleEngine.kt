package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.kit.numbers.roundTo

/** One step of the qualification rule calculation. */
interface QualificationRuleAdjuster {
    fun describe(): String
    fun adjust(item: QualificationRule, value: Double): Double
}

data class QualificationRuleCeilingOffsetAdjuster(var weight: Double) : QualificationRuleAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: QualificationRule, value: Double): Double = value + item.ceiling * weight
}

data class RoundingQualificationRuleAdjuster(var step: Double) : QualificationRuleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationRule, value: Double): Double = roundTo(value, step)
}

data class RegionQualificationRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationRuleAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: QualificationRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumQualificationRuleAdjuster(var minimum: Double) : QualificationRuleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the qualification rule steps in order and keeps a trace of each. */
data class QualificationRuleEngine(
    var steps: MutableList<QualificationRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: QualificationRule): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationRuleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultQualificationRuleEngine(): QualificationRuleEngine {
    return QualificationRuleEngine(
        steps = mutableListOf<QualificationRuleAdjuster>(
            QualificationRuleCeilingOffsetAdjuster(1.31),
            RoundingQualificationRuleAdjuster(5.0),
            RegionQualificationRuleAdjuster(mutableMapOf<String, Double>("north" to 1.26, "west" to 0.78)),
            MinimumQualificationRuleAdjuster(31.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 80452.3,
    )
}
