package com.acme.transport.network.node.nodedraft

/** One step of the legacy node plan calculation. */
interface LegacyNodePlanStep {
    fun describe(): String
    fun adjust(item: LegacyNodePlan, value: Double): Double
}

data class PercentLegacyNodePlanAdjuster(var percent: Double) : LegacyNodePlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyNodePlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryLegacyNodePlanAdjuster(var factors: MutableMap<String, Double>) : LegacyNodePlanStep {
    override fun describe(): String = "by country"

    override fun adjust(item: LegacyNodePlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LegacyNodePlanMaximumOffsetAdjuster(var weight: Double) : LegacyNodePlanStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: LegacyNodePlan, value: Double): Double = value + item.maximum * weight
}

/** Runs the legacy node plan steps in order and keeps a trace of each. */
data class LegacyNodePlanEngine(
    var steps: MutableList<LegacyNodePlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyNodePlan): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyNodePlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyNodePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyNodePlanEngine(): LegacyNodePlanEngine {
    return LegacyNodePlanEngine(
        steps = mutableListOf<LegacyNodePlanStep>(
            PercentLegacyNodePlanAdjuster(-1.3),
            CountryLegacyNodePlanAdjuster(mutableMapOf<String, Double>("west" to 0.86)),
            LegacyNodePlanMaximumOffsetAdjuster(0.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 42265.2,
    )
}
