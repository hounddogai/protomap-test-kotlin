package com.acme.transport.customs.clearance.cycle

import com.acme.transport.kit.flags.Registry

/** One step of the clearance entry calculation. */
interface ClearanceEntryStep {
    fun describe(): String
    fun adjust(item: ClearanceEntry, value: Double): Double
}

data class MinimumClearanceEntryAdjuster(var minimum: Double) : ClearanceEntryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearanceEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedClearanceEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearanceEntryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearanceEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentClearanceEntryAdjuster(var percent: Double) : ClearanceEntryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearanceEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the clearance entry steps in order and keeps a trace of each. */
data class ClearanceEntryEngine(
    var steps: MutableList<ClearanceEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ClearanceEntry): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceEntryStep = steps[index]
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

    fun evaluateAll(items: MutableList<ClearanceEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultClearanceEntryEngine(flags: Registry): ClearanceEntryEngine {
    return ClearanceEntryEngine(
        steps = mutableListOf<ClearanceEntryStep>(
            MinimumClearanceEntryAdjuster(49.5),
            FlaggedClearanceEntryAdjuster(flags, "clearance-entry-batched", 8.9),
            PercentClearanceEntryAdjuster(-21.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 72303.1,
    )
}
