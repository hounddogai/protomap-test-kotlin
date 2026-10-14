package com.acme.transport.terminal.crossdock.entry

import com.acme.transport.kit.flags.Registry

/** One step of the batched cross dock entry calculation. */
interface BatchedCrossDockEntryStep {
    fun describe(): String
    fun adjust(item: BatchedCrossDockEntry, value: Double): Double
}

data class TieredBatchedCrossDockEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedCrossDockEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedCrossDockEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class BatchedCrossDockEntryScoreOffsetAdjuster(var weight: Double) : BatchedCrossDockEntryStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: BatchedCrossDockEntry, value: Double): Double = value + item.score * weight
}

data class FlaggedBatchedCrossDockEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedCrossDockEntryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedCrossDockEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched cross dock entry steps in order and keeps a trace of each. */
data class BatchedCrossDockEntryEngine(
    var steps: MutableList<BatchedCrossDockEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedCrossDockEntry): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedCrossDockEntryStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedCrossDockEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedCrossDockEntryEngine(flags: Registry): BatchedCrossDockEntryEngine {
    return BatchedCrossDockEntryEngine(
        steps = mutableListOf<BatchedCrossDockEntryStep>(
            TieredBatchedCrossDockEntryAdjuster(
                mutableListOf<Double>(4322.0, 9873.0),
                mutableListOf<Double>(0.86, 1.06),
            ),
            BatchedCrossDockEntryScoreOffsetAdjuster(1.08),
            FlaggedBatchedCrossDockEntryAdjuster(flags, "batched-cross-dock-entry-capped", 5.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 63984.9,
    )
}
