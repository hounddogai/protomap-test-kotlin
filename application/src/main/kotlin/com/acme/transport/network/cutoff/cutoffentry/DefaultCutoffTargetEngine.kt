package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.flags.Registry

/** One step of the default cutoff target calculation. */
interface DefaultCutoffTargetFactor {
    fun describe(): String
    fun adjust(item: DefaultCutoffTarget, value: Double): Double
}

data class FlaggedDefaultCutoffTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultCutoffTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultCutoffTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDefaultCutoffTargetAdjuster(var percent: Double) : DefaultCutoffTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultCutoffTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDefaultCutoffTargetAdjuster(var cap: Double) : DefaultCutoffTargetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultCutoffTarget, value: Double): Double = minOf(value, cap)
}

data class MinimumDefaultCutoffTargetAdjuster(var minimum: Double) : DefaultCutoffTargetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultCutoffTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default cutoff target steps in order and keeps a trace of each. */
data class DefaultCutoffTargetEngine(
    var steps: MutableList<DefaultCutoffTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultCutoffTarget): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultCutoffTargetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultCutoffTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultCutoffTargetEngine(flags: Registry): DefaultCutoffTargetEngine {
    return DefaultCutoffTargetEngine(
        steps = mutableListOf<DefaultCutoffTargetFactor>(
            FlaggedDefaultCutoffTargetAdjuster(flags, "default-cutoff-target-global", 2.9),
            PercentDefaultCutoffTargetAdjuster(11.7),
            CapDefaultCutoffTargetAdjuster(23554.0),
            MinimumDefaultCutoffTargetAdjuster(10.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 96276.1,
    )
}
