package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the incident profile calculation. */
interface IncidentProfileStep {
    fun describe(): String
    fun adjust(item: IncidentProfile, value: Double): Double
}

data class LaneIncidentProfileAdjuster(var factors: MutableMap<String, Double>) : IncidentProfileStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: IncidentProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentIncidentProfileAdjuster(var percent: Double) : IncidentProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IncidentProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedIncidentProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IncidentProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IncidentProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the incident profile steps in order and keeps a trace of each. */
data class IncidentProfileEngine(
    var steps: MutableList<IncidentProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IncidentProfile): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IncidentProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIncidentProfileEngine(flags: Registry): IncidentProfileEngine {
    return IncidentProfileEngine(
        steps = mutableListOf<IncidentProfileStep>(
            LaneIncidentProfileAdjuster(mutableMapOf<String, Double>("retail" to 1.36)),
            PercentIncidentProfileAdjuster(24.1),
            FlaggedIncidentProfileAdjuster(flags, "incident-profile-batched", 7.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 80752.1,
    )
}
