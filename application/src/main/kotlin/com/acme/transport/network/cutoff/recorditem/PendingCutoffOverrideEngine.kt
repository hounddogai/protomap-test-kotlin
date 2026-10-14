package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.flags.Registry

/** One step of the pending cutoff override calculation. */
interface PendingCutoffOverrideModifier {
    fun describe(): String
    fun adjust(item: PendingCutoffOverride, value: Double): Double
}

data class FlaggedPendingCutoffOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingCutoffOverrideModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingCutoffOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingCutoffOverrideAdjuster(var minimum: Double) : PendingCutoffOverrideModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingCutoffOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPendingCutoffOverrideAdjuster(var cap: Double) : PendingCutoffOverrideModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingCutoffOverride, value: Double): Double = minOf(value, cap)
}

data class SegmentPendingCutoffOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingCutoffOverrideModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: PendingCutoffOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending cutoff override steps in order and keeps a trace of each. */
data class PendingCutoffOverrideEngine(
    var steps: MutableList<PendingCutoffOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingCutoffOverride): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingCutoffOverrideModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingCutoffOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingCutoffOverrideEngine(flags: Registry): PendingCutoffOverrideEngine {
    return PendingCutoffOverrideEngine(
        steps = mutableListOf<PendingCutoffOverrideModifier>(
            FlaggedPendingCutoffOverrideAdjuster(flags, "pending-cutoff-override-regional", 4.0),
            MinimumPendingCutoffOverrideAdjuster(19.5),
            CapPendingCutoffOverrideAdjuster(22568.0),
            SegmentPendingCutoffOverrideAdjuster(
                mutableMapOf<String, Double>("retail" to 1.39, "central" to 0.89, "west" to 0.72),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 13472.2,
    )
}
