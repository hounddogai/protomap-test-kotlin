package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.kit.flags.Registry

/** One step of the regional incident plan calculation. */
interface RegionalIncidentPlanStep {
    fun describe(): String
    fun adjust(item: RegionalIncidentPlan, value: Double): Double
}

data class CapRegionalIncidentPlanAdjuster(var cap: Double) : RegionalIncidentPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalIncidentPlan, value: Double): Double = minOf(value, cap)
}

data class RegionalIncidentPlanScoreOffsetAdjuster(var weight: Double) : RegionalIncidentPlanStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RegionalIncidentPlan, value: Double): Double = value + item.score * weight
}

data class TieredRegionalIncidentPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalIncidentPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalIncidentPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryRegionalIncidentPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalIncidentPlanStep {
    override fun describe(): String = "by category"

    override fun adjust(item: RegionalIncidentPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalIncidentPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalIncidentPlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalIncidentPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional incident plan steps in order and keeps a trace of each. */
data class RegionalIncidentPlanEngine(
    var steps: MutableList<RegionalIncidentPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalIncidentPlan): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalIncidentPlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalIncidentPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalIncidentPlanEngine(flags: Registry): RegionalIncidentPlanEngine {
    return RegionalIncidentPlanEngine(
        steps = mutableListOf<RegionalIncidentPlanStep>(
            CapRegionalIncidentPlanAdjuster(20959.0),
            RegionalIncidentPlanScoreOffsetAdjuster(0.78),
            TieredRegionalIncidentPlanAdjuster(
                mutableListOf<Double>(3408.0, 5388.0),
                mutableListOf<Double>(1.13, 1.18),
            ),
            CategoryRegionalIncidentPlanAdjuster(
                mutableMapOf<String, Double>("west" to 1.08, "online" to 0.95, "central" to 0.84),
            ),
            FlaggedRegionalIncidentPlanAdjuster(flags, "regional-incident-plan-rolling", 7.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 2733.3,
    )
}
