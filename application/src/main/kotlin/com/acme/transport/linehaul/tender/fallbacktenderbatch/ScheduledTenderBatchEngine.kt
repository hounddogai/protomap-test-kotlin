package com.acme.transport.linehaul.tender.fallbacktenderbatch

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled tender batch calculation. */
interface ScheduledTenderBatchAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledTenderBatch, value: Double): Double
}

data class FlaggedScheduledTenderBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledTenderBatchAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledTenderBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredScheduledTenderBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledTenderBatchAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledTenderBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ScheduledTenderBatchUpdatedAtOffsetAdjuster(var weight: Double) : ScheduledTenderBatchAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: ScheduledTenderBatch, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

/** Runs the scheduled tender batch steps in order and keeps a trace of each. */
data class ScheduledTenderBatchEngine(
    var steps: MutableList<ScheduledTenderBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledTenderBatch): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledTenderBatchAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledTenderBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledTenderBatchEngine(flags: Registry): ScheduledTenderBatchEngine {
    return ScheduledTenderBatchEngine(
        steps = mutableListOf<ScheduledTenderBatchAdjuster>(
            FlaggedScheduledTenderBatchAdjuster(flags, "scheduled-tender-batch-primary", -2.8),
            TieredScheduledTenderBatchAdjuster(
                mutableListOf<Double>(1519.0, 3713.0, 4384.0, 7750.0),
                mutableListOf<Double>(1.07, 0.91, 0.98, 0.88),
            ),
            ScheduledTenderBatchUpdatedAtOffsetAdjuster(1.58),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 50220.3,
    )
}
