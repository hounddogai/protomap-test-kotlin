package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.flags.Registry

/** One step of the strict attempt override calculation. */
interface StrictAttemptOverrideStep {
    fun describe(): String
    fun adjust(item: StrictAttemptOverride, value: Double): Double
}

data class StrictAttemptOverridePriorityOffsetAdjuster(var weight: Double) : StrictAttemptOverrideStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: StrictAttemptOverride, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class FlaggedStrictAttemptOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictAttemptOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictAttemptOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelStrictAttemptOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictAttemptOverrideStep {
    override fun describe(): String = "by label"

    override fun adjust(item: StrictAttemptOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict attempt override steps in order and keeps a trace of each. */
data class StrictAttemptOverrideEngine(
    var steps: MutableList<StrictAttemptOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictAttemptOverride): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictAttemptOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictAttemptOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictAttemptOverrideEngine(flags: Registry): StrictAttemptOverrideEngine {
    return StrictAttemptOverrideEngine(
        steps = mutableListOf<StrictAttemptOverrideStep>(
            StrictAttemptOverridePriorityOffsetAdjuster(0.12),
            FlaggedStrictAttemptOverrideAdjuster(flags, "strict-attempt-override-default", -5.0),
            LabelStrictAttemptOverrideAdjuster(
                mutableMapOf<String, Double>("online" to 1.07, "south" to 1.06, "north" to 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 51897.9,
    )
}
