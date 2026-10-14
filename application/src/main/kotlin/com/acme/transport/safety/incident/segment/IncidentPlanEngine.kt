package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.numbers.roundTo

/** One step of the incident plan calculation. */
interface IncidentPlanStep {
    fun describe(): String
    fun adjust(item: IncidentPlan, value: Double): Double
}

data class TieredIncidentPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapIncidentPlanAdjuster(var cap: Double) : IncidentPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IncidentPlan, value: Double): Double = minOf(value, cap)
}

data class RoundingIncidentPlanAdjuster(var step: Double) : IncidentPlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IncidentPlan, value: Double): Double = roundTo(value, step)
}

data class PercentIncidentPlanAdjuster(var percent: Double) : IncidentPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IncidentPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryIncidentPlanAdjuster(var factors: MutableMap<String, Double>) : IncidentPlanStep {
    override fun describe(): String = "by country"

    override fun adjust(item: IncidentPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the incident plan steps in order and keeps a trace of each. */
data class IncidentPlanEngine(
    var steps: MutableList<IncidentPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: IncidentPlan): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IncidentPlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<IncidentPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultIncidentPlanEngine(): IncidentPlanEngine {
    return IncidentPlanEngine(
        steps = mutableListOf<IncidentPlanStep>(
            TieredIncidentPlanAdjuster(
                mutableListOf<Double>(1572.0, 2496.0, 6981.0, 8565.0),
                mutableListOf<Double>(0.81, 1.08, 0.92, 1.13),
            ),
            CapIncidentPlanAdjuster(48873.0),
            RoundingIncidentPlanAdjuster(0.05),
            PercentIncidentPlanAdjuster(16.0),
            CountryIncidentPlanAdjuster(
                mutableMapOf<String, Double>("retail" to 1.23, "central" to 1.02, "west" to 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 57738.7,
    )
}
