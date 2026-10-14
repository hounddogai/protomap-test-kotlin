package com.acme.transport.terminal.dockdoor.doorestimate

/** One step of the default dock door preference calculation. */
interface DefaultDockDoorPreferenceStep {
    fun describe(): String
    fun adjust(item: DefaultDockDoorPreference, value: Double): Double
}

data class MinimumDefaultDockDoorPreferenceAdjuster(var minimum: Double) : DefaultDockDoorPreferenceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultDockDoorPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DefaultDockDoorPreferenceScoreOffsetAdjuster(var weight: Double) : DefaultDockDoorPreferenceStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: DefaultDockDoorPreference, value: Double): Double = value + item.score * weight
}

data class CapDefaultDockDoorPreferenceAdjuster(var cap: Double) : DefaultDockDoorPreferenceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultDockDoorPreference, value: Double): Double = minOf(value, cap)
}

/** Runs the default dock door preference steps in order and keeps a trace of each. */
data class DefaultDockDoorPreferenceEngine(
    var steps: MutableList<DefaultDockDoorPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultDockDoorPreference): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultDockDoorPreferenceStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultDockDoorPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultDockDoorPreferenceEngine(): DefaultDockDoorPreferenceEngine {
    return DefaultDockDoorPreferenceEngine(
        steps = mutableListOf<DefaultDockDoorPreferenceStep>(
            MinimumDefaultDockDoorPreferenceAdjuster(19.9),
            DefaultDockDoorPreferenceScoreOffsetAdjuster(1.28),
            CapDefaultDockDoorPreferenceAdjuster(43711.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 66558.9,
    )
}
