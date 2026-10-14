package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary incident override calculation. */
interface PrimaryIncidentOverrideFactor {
    fun describe(): String
    fun adjust(item: PrimaryIncidentOverride, value: Double): Double
}

data class RoundingPrimaryIncidentOverrideAdjuster(var step: Double) : PrimaryIncidentOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryIncidentOverride, value: Double): Double = roundTo(value, step)
}

data class MinimumPrimaryIncidentOverrideAdjuster(var minimum: Double) : PrimaryIncidentOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryIncidentOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPrimaryIncidentOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryIncidentOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryIncidentOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the primary incident override steps in order and keeps a trace of each. */
data class PrimaryIncidentOverrideEngine(
    var steps: MutableList<PrimaryIncidentOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryIncidentOverride): Double {
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

    fun evaluateAll(items: MutableList<PrimaryIncidentOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryIncidentOverrideEngine(flags: Registry): PrimaryIncidentOverrideEngine {
    return PrimaryIncidentOverrideEngine(
        steps = mutableListOf<PrimaryIncidentOverrideFactor>(
            RoundingPrimaryIncidentOverrideAdjuster(0.05),
            MinimumPrimaryIncidentOverrideAdjuster(21.0),
            FlaggedPrimaryIncidentOverrideAdjuster(flags, "primary-incident-override-global", 2.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 95360.8,
    )
}
