package com.acme.transport.safety.incident.incidentclaim

/** One step of the incident policy calculation. */
interface IncidentPolicyAdjuster {
    fun describe(): String
    fun adjust(item: IncidentPolicy, value: Double): Double
}

data class LaneIncidentPolicyAdjuster(var factors: MutableMap<String, Double>) : IncidentPolicyAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: IncidentPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapIncidentPolicyAdjuster(var cap: Double) : IncidentPolicyAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IncidentPolicy, value: Double): Double = minOf(value, cap)
}

data class MinimumIncidentPolicyAdjuster(var minimum: Double) : IncidentPolicyAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IncidentPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the incident policy steps in order and keeps a trace of each. */
data class IncidentPolicyEngine(
    var steps: MutableList<IncidentPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: IncidentPolicy): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IncidentPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<IncidentPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultIncidentPolicyEngine(): IncidentPolicyEngine {
    return IncidentPolicyEngine(
        steps = mutableListOf<IncidentPolicyAdjuster>(
            LaneIncidentPolicyAdjuster(
                mutableMapOf<String, Double>("east" to 0.76, "south" to 1.14, "retail" to 0.93),
            ),
            CapIncidentPolicyAdjuster(19334.0),
            MinimumIncidentPolicyAdjuster(38.5),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 14990.8,
    )
}
