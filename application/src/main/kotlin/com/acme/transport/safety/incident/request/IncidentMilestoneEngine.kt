package com.acme.transport.safety.incident.request

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the incident milestone calculation. */
interface IncidentMilestoneModifier {
    fun describe(): String
    fun adjust(item: IncidentMilestone, value: Double): Double
}

data class DepotIncidentMilestoneAdjuster(var factors: MutableMap<String, Double>) : IncidentMilestoneModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: IncidentMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentIncidentMilestoneAdjuster(var percent: Double) : IncidentMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IncidentMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingIncidentMilestoneAdjuster(var step: Double) : IncidentMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IncidentMilestone, value: Double): Double = roundTo(value, step)
}

/** Runs the incident milestone steps in order and keeps a trace of each. */
data class IncidentMilestoneEngine(
    var steps: MutableList<IncidentMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IncidentMilestone): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IncidentMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIncidentMilestoneEngine(): IncidentMilestoneEngine {
    return IncidentMilestoneEngine(
        steps = mutableListOf<IncidentMilestoneModifier>(
            DepotIncidentMilestoneAdjuster(
                mutableMapOf<String, Double>("central" to 1.2, "west" to 1.27, "online" to 0.82),
            ),
            PercentIncidentMilestoneAdjuster(23.6),
            RoundingIncidentMilestoneAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 82717.7,
    )
}
