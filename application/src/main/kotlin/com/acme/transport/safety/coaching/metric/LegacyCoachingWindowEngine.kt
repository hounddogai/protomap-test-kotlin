package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.flags.Registry

/** One step of the legacy coaching window calculation. */
interface LegacyCoachingWindowStep {
    fun describe(): String
    fun adjust(item: LegacyCoachingWindow, value: Double): Double
}

data class FlaggedLegacyCoachingWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyCoachingWindowStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyCoachingWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLegacyCoachingWindowAdjuster(var minimum: Double) : LegacyCoachingWindowStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyCoachingWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LegacyCoachingWindowLimitOffsetAdjuster(var weight: Double) : LegacyCoachingWindowStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: LegacyCoachingWindow, value: Double): Double = value + item.limit.toDouble() * weight
}

/** Runs the legacy coaching window steps in order and keeps a trace of each. */
data class LegacyCoachingWindowEngine(
    var steps: MutableList<LegacyCoachingWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyCoachingWindow): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyCoachingWindowStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyCoachingWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyCoachingWindowEngine(flags: Registry): LegacyCoachingWindowEngine {
    return LegacyCoachingWindowEngine(
        steps = mutableListOf<LegacyCoachingWindowStep>(
            FlaggedLegacyCoachingWindowAdjuster(flags, "legacy-coaching-window-capped", -2.3),
            MinimumLegacyCoachingWindowAdjuster(18.5),
            LegacyCoachingWindowLimitOffsetAdjuster(0.77),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 71911.6,
    )
}
