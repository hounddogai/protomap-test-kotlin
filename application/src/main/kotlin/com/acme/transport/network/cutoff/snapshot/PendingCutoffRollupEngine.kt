package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending cutoff rollup calculation. */
interface PendingCutoffRollupStep {
    fun describe(): String
    fun adjust(item: PendingCutoffRollup, value: Double): Double
}

data class MinimumPendingCutoffRollupAdjuster(var minimum: Double) : PendingCutoffRollupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingCutoffRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelPendingCutoffRollupAdjuster(var factors: MutableMap<String, Double>) : PendingCutoffRollupStep {
    override fun describe(): String = "by label"

    override fun adjust(item: PendingCutoffRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPendingCutoffRollupAdjuster(var percent: Double) : PendingCutoffRollupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingCutoffRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PendingCutoffRollupExpiresAtOffsetAdjuster(var weight: Double) : PendingCutoffRollupStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: PendingCutoffRollup, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class RoundingPendingCutoffRollupAdjuster(var step: Double) : PendingCutoffRollupStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingCutoffRollup, value: Double): Double = roundTo(value, step)
}

data class CapPendingCutoffRollupAdjuster(var cap: Double) : PendingCutoffRollupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingCutoffRollup, value: Double): Double = minOf(value, cap)
}

/** Runs the pending cutoff rollup steps in order and keeps a trace of each. */
data class PendingCutoffRollupEngine(
    var steps: MutableList<PendingCutoffRollupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingCutoffRollup): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingCutoffRollupStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingCutoffRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingCutoffRollupEngine(): PendingCutoffRollupEngine {
    return PendingCutoffRollupEngine(
        steps = mutableListOf<PendingCutoffRollupStep>(
            MinimumPendingCutoffRollupAdjuster(22.1),
            LabelPendingCutoffRollupAdjuster(mutableMapOf<String, Double>("central" to 1.14, "east" to 0.7)),
            PercentPendingCutoffRollupAdjuster(-3.3),
            PendingCutoffRollupExpiresAtOffsetAdjuster(0.83),
            RoundingPendingCutoffRollupAdjuster(1.0),
            CapPendingCutoffRollupAdjuster(44558.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 61384.4,
    )
}
