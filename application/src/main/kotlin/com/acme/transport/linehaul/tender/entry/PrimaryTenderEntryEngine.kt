package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.flags.Registry

/** One step of the primary tender entry calculation. */
interface PrimaryTenderEntryStep {
    fun describe(): String
    fun adjust(item: PrimaryTenderEntry, value: Double): Double
}

data class FlaggedPrimaryTenderEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryTenderEntryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryTenderEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitlePrimaryTenderEntryAdjuster(var factors: MutableMap<String, Double>) : PrimaryTenderEntryStep {
    override fun describe(): String = "by title"

    override fun adjust(item: PrimaryTenderEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPrimaryTenderEntryAdjuster(var percent: Double) : PrimaryTenderEntryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryTenderEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPrimaryTenderEntryAdjuster(var minimum: Double) : PrimaryTenderEntryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryTenderEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPrimaryTenderEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryTenderEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryTenderEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary tender entry steps in order and keeps a trace of each. */
data class PrimaryTenderEntryEngine(
    var steps: MutableList<PrimaryTenderEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryTenderEntry): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryTenderEntryStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryTenderEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryTenderEntryEngine(flags: Registry): PrimaryTenderEntryEngine {
    return PrimaryTenderEntryEngine(
        steps = mutableListOf<PrimaryTenderEntryStep>(
            FlaggedPrimaryTenderEntryAdjuster(flags, "primary-tender-entry-lenient", 4.6),
            TitlePrimaryTenderEntryAdjuster(mutableMapOf<String, Double>("east" to 1.3, "south" to 1.21)),
            PercentPrimaryTenderEntryAdjuster(-14.0),
            MinimumPrimaryTenderEntryAdjuster(9.6),
            TieredPrimaryTenderEntryAdjuster(
                mutableListOf<Double>(1457.0, 3302.0, 9082.0),
                mutableListOf<Double>(1.08, 1.08, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 67472.1,
    )
}
