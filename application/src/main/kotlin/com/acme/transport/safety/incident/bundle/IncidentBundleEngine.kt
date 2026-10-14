package com.acme.transport.safety.incident.bundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the incident bundle calculation. */
interface IncidentBundleModifier {
    fun describe(): String
    fun adjust(item: IncidentBundle, value: Double): Double
}

data class LocaleIncidentBundleAdjuster(var factors: MutableMap<String, Double>) : IncidentBundleModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: IncidentBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingIncidentBundleAdjuster(var step: Double) : IncidentBundleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IncidentBundle, value: Double): Double = roundTo(value, step)
}

data class MinimumIncidentBundleAdjuster(var minimum: Double) : IncidentBundleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IncidentBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the incident bundle steps in order and keeps a trace of each. */
data class IncidentBundleEngine(
    var steps: MutableList<IncidentBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IncidentBundle): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IncidentBundleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<IncidentBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIncidentBundleEngine(): IncidentBundleEngine {
    return IncidentBundleEngine(
        steps = mutableListOf<IncidentBundleModifier>(
            LocaleIncidentBundleAdjuster(mutableMapOf<String, Double>("east" to 1.36)),
            RoundingIncidentBundleAdjuster(5.0),
            MinimumIncidentBundleAdjuster(47.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 35753.6,
    )
}
