package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.kit.flags.Registry

/** One step of the idling summary calculation. */
interface IdlingSummaryStep {
    fun describe(): String
    fun adjust(item: IdlingSummary, value: Double): Double
}

data class MinimumIdlingSummaryAdjuster(var minimum: Double) : IdlingSummaryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapIdlingSummaryAdjuster(var cap: Double) : IdlingSummaryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingSummary, value: Double): Double = minOf(value, cap)
}

data class FlaggedIdlingSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingSummaryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the idling summary steps in order and keeps a trace of each. */
data class IdlingSummaryEngine(
    var steps: MutableList<IdlingSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IdlingSummary): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingSummaryStep = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIdlingSummaryEngine(flags: Registry): IdlingSummaryEngine {
    return IdlingSummaryEngine(
        steps = mutableListOf<IdlingSummaryStep>(
            MinimumIdlingSummaryAdjuster(45.0),
            CapIdlingSummaryAdjuster(18059.0),
            FlaggedIdlingSummaryAdjuster(flags, "idling-summary-batched", -0.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 91878.3,
    )
}
