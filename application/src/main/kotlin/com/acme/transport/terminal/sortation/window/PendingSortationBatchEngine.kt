package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending sortation batch calculation. */
interface PendingSortationBatchModifier {
    fun describe(): String
    fun adjust(item: PendingSortationBatch, value: Double): Double
}

data class RoundingPendingSortationBatchAdjuster(var step: Double) : PendingSortationBatchModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingSortationBatch, value: Double): Double = roundTo(value, step)
}

data class PercentPendingSortationBatchAdjuster(var percent: Double) : PendingSortationBatchModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingSortationBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PendingSortationBatchCeilingOffsetAdjuster(var weight: Double) : PendingSortationBatchModifier {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: PendingSortationBatch, value: Double): Double = value + item.ceiling * weight
}

data class FlaggedPendingSortationBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingSortationBatchModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingSortationBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending sortation batch steps in order and keeps a trace of each. */
data class PendingSortationBatchEngine(
    var steps: MutableList<PendingSortationBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingSortationBatch): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingSortationBatchModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingSortationBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingSortationBatchEngine(flags: Registry): PendingSortationBatchEngine {
    return PendingSortationBatchEngine(
        steps = mutableListOf<PendingSortationBatchModifier>(
            RoundingPendingSortationBatchAdjuster(0.05),
            PercentPendingSortationBatchAdjuster(-16.2),
            PendingSortationBatchCeilingOffsetAdjuster(0.42),
            FlaggedPendingSortationBatchAdjuster(flags, "pending-sortation-batch-primary", -6.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 19767.0,
    )
}
