package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the incident request calculation. */
interface IncidentRequestStep {
    fun describe(): String
    fun adjust(item: IncidentRequest, value: Double): Double
}

data class FlaggedIncidentRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IncidentRequestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IncidentRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class IncidentRequestWindowDaysOffsetAdjuster(var weight: Double) : IncidentRequestStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: IncidentRequest, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class MinimumIncidentRequestAdjuster(var minimum: Double) : IncidentRequestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IncidentRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the incident request steps in order and keeps a trace of each. */
data class IncidentRequestEngine(
    var steps: MutableList<IncidentRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IncidentRequest): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IncidentRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIncidentRequestEngine(flags: Registry): IncidentRequestEngine {
    return IncidentRequestEngine(
        steps = mutableListOf<IncidentRequestStep>(
            FlaggedIncidentRequestAdjuster(flags, "incident-request-primary", 8.1),
            IncidentRequestWindowDaysOffsetAdjuster(0.61),
            MinimumIncidentRequestAdjuster(20.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 76404.2,
    )
}
