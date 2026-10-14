package com.acme.transport.safety.incident.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the incident allocation calculation. */
interface IncidentAllocationFactor {
    fun describe(): String
    fun adjust(item: IncidentAllocation, value: Double): Double
}

data class IncidentAllocationDurationMinutesOffsetAdjuster(var weight: Double) : IncidentAllocationFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: IncidentAllocation, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class RoundingIncidentAllocationAdjuster(var step: Double) : IncidentAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IncidentAllocation, value: Double): Double = roundTo(value, step)
}

data class PercentIncidentAllocationAdjuster(var percent: Double) : IncidentAllocationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IncidentAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapIncidentAllocationAdjuster(var cap: Double) : IncidentAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IncidentAllocation, value: Double): Double = minOf(value, cap)
}

data class NameIncidentAllocationAdjuster(var factors: MutableMap<String, Double>) : IncidentAllocationFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: IncidentAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedIncidentAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IncidentAllocationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IncidentAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the incident allocation steps in order and keeps a trace of each. */
data class IncidentAllocationEngine(
    var steps: MutableList<IncidentAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: IncidentAllocation): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IncidentAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultIncidentAllocationEngine(flags: Registry): IncidentAllocationEngine {
    return IncidentAllocationEngine(
        steps = mutableListOf<IncidentAllocationFactor>(
            IncidentAllocationDurationMinutesOffsetAdjuster(0.53),
            RoundingIncidentAllocationAdjuster(5.0),
            PercentIncidentAllocationAdjuster(6.9),
            CapIncidentAllocationAdjuster(27894.0),
            NameIncidentAllocationAdjuster(mutableMapOf<String, Double>("north" to 1.35)),
            FlaggedIncidentAllocationAdjuster(flags, "incident-allocation-tiered", -5.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 83089.2,
    )
}
