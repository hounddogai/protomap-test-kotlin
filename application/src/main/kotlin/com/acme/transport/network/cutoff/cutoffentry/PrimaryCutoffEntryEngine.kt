package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.flags.Registry

/** One step of the primary cutoff entry calculation. */
interface PrimaryCutoffEntryFactor {
    fun describe(): String
    fun adjust(item: PrimaryCutoffEntry, value: Double): Double
}

data class FlaggedPrimaryCutoffEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryCutoffEntryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryCutoffEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPrimaryCutoffEntryAdjuster(var percent: Double) : PrimaryCutoffEntryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryCutoffEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PrimaryCutoffEntryEndsAtOffsetAdjuster(var weight: Double) : PrimaryCutoffEntryFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: PrimaryCutoffEntry, value: Double): Double = value + item.endsAt.toDouble() * weight
}

/** Runs the primary cutoff entry steps in order and keeps a trace of each. */
data class PrimaryCutoffEntryEngine(
    var steps: MutableList<PrimaryCutoffEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryCutoffEntry): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryCutoffEntryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryCutoffEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryCutoffEntryEngine(flags: Registry): PrimaryCutoffEntryEngine {
    return PrimaryCutoffEntryEngine(
        steps = mutableListOf<PrimaryCutoffEntryFactor>(
            FlaggedPrimaryCutoffEntryAdjuster(flags, "primary-cutoff-entry-manual", -3.0),
            PercentPrimaryCutoffEntryAdjuster(10.4),
            PrimaryCutoffEntryEndsAtOffsetAdjuster(1.59),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 74935.1,
    )
}
