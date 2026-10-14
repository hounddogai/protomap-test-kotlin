package com.acme.transport.safety.incident.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the incident batch calculation. */
interface IncidentBatchStep {
    fun describe(): String
    fun adjust(item: IncidentBatch, value: Double): Double
}

data class FlaggedIncidentBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IncidentBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IncidentBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingIncidentBatchAdjuster(var step: Double) : IncidentBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IncidentBatch, value: Double): Double = roundTo(value, step)
}

data class MinimumIncidentBatchAdjuster(var minimum: Double) : IncidentBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IncidentBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the incident batch steps in order and keeps a trace of each. */
data class IncidentBatchEngine(
    var steps: MutableList<IncidentBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: IncidentBatch): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IncidentBatchStep = steps[index]
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

    fun evaluateAll(items: MutableList<IncidentBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultIncidentBatchEngine(flags: Registry): IncidentBatchEngine {
    return IncidentBatchEngine(
        steps = mutableListOf<IncidentBatchStep>(
            FlaggedIncidentBatchAdjuster(flags, "incident-batch-rolling", 1.7),
            RoundingIncidentBatchAdjuster(1.0),
            MinimumIncidentBatchAdjuster(29.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 14245.5,
    )
}
