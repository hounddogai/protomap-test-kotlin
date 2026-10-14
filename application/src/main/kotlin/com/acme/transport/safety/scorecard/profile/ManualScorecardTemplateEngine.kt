package com.acme.transport.safety.scorecard.profile

/** One step of the manual scorecard template calculation. */
interface ManualScorecardTemplateModifier {
    fun describe(): String
    fun adjust(item: ManualScorecardTemplate, value: Double): Double
}

data class CapManualScorecardTemplateAdjuster(var cap: Double) : ManualScorecardTemplateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualScorecardTemplate, value: Double): Double = minOf(value, cap)
}

data class DepotManualScorecardTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualScorecardTemplateModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: ManualScorecardTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ManualScorecardTemplateMinimumOffsetAdjuster(var weight: Double) : ManualScorecardTemplateModifier {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: ManualScorecardTemplate, value: Double): Double = value + item.minimum * weight
}

/** Runs the manual scorecard template steps in order and keeps a trace of each. */
data class ManualScorecardTemplateEngine(
    var steps: MutableList<ManualScorecardTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualScorecardTemplate): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualScorecardTemplateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualScorecardTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualScorecardTemplateEngine(): ManualScorecardTemplateEngine {
    return ManualScorecardTemplateEngine(
        steps = mutableListOf<ManualScorecardTemplateModifier>(
            CapManualScorecardTemplateAdjuster(18534.0),
            DepotManualScorecardTemplateAdjuster(mutableMapOf<String, Double>("north" to 0.76)),
            ManualScorecardTemplateMinimumOffsetAdjuster(1.41),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 79672.6,
    )
}
