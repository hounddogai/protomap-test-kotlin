package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending incident request calculation. */
interface PendingIncidentRequestFactor {
    fun describe(): String
    fun adjust(item: PendingIncidentRequest, value: Double): Double
}

data class RoundingPendingIncidentRequestAdjuster(var step: Double) : PendingIncidentRequestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingIncidentRequest, value: Double): Double = roundTo(value, step)
}

data class CapPendingIncidentRequestAdjuster(var cap: Double) : PendingIncidentRequestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingIncidentRequest, value: Double): Double = minOf(value, cap)
}

data class PercentPendingIncidentRequestAdjuster(var percent: Double) : PendingIncidentRequestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingIncidentRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingIncidentRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingIncidentRequestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingIncidentRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingIncidentRequestAdjuster(var minimum: Double) : PendingIncidentRequestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingIncidentRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending incident request steps in order and keeps a trace of each. */
data class PendingIncidentRequestEngine(
    var steps: MutableList<PendingIncidentRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingIncidentRequest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingIncidentRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingIncidentRequestEngine(flags: Registry): PendingIncidentRequestEngine {
    return PendingIncidentRequestEngine(
        steps = mutableListOf<PendingIncidentRequestFactor>(
            RoundingPendingIncidentRequestAdjuster(0.5),
            CapPendingIncidentRequestAdjuster(19111.0),
            PercentPendingIncidentRequestAdjuster(-19.9),
            FlaggedPendingIncidentRequestAdjuster(flags, "pending-incident-request-pending", 3.1),
            MinimumPendingIncidentRequestAdjuster(36.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 21417.0,
    )
}
