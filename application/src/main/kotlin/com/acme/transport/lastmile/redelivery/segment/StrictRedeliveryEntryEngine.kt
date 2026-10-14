package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.flags.Registry

/** One step of the strict redelivery entry calculation. */
interface StrictRedeliveryEntryStep {
    fun describe(): String
    fun adjust(item: StrictRedeliveryEntry, value: Double): Double
}

data class SourceStrictRedeliveryEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictRedeliveryEntryStep {
    override fun describe(): String = "by source"

    override fun adjust(item: StrictRedeliveryEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedStrictRedeliveryEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictRedeliveryEntryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictRedeliveryEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class StrictRedeliveryEntryBatchSizeOffsetAdjuster(var weight: Double) : StrictRedeliveryEntryStep {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: StrictRedeliveryEntry, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

/** Runs the strict redelivery entry steps in order and keeps a trace of each. */
data class StrictRedeliveryEntryEngine(
    var steps: MutableList<StrictRedeliveryEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictRedeliveryEntry): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictRedeliveryEntryStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictRedeliveryEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictRedeliveryEntryEngine(flags: Registry): StrictRedeliveryEntryEngine {
    return StrictRedeliveryEntryEngine(
        steps = mutableListOf<StrictRedeliveryEntryStep>(
            SourceStrictRedeliveryEntryAdjuster(mutableMapOf<String, Double>("east" to 0.93, "central" to 1.2)),
            FlaggedStrictRedeliveryEntryAdjuster(flags, "strict-redelivery-entry-tiered", -3.0),
            StrictRedeliveryEntryBatchSizeOffsetAdjuster(0.28),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 85641.8,
    )
}
