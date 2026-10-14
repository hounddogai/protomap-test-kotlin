package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary drop entry calculation. */
interface PrimaryDropEntryFactor {
    fun describe(): String
    fun adjust(item: PrimaryDropEntry, value: Double): Double
}

data class PercentPrimaryDropEntryAdjuster(var percent: Double) : PrimaryDropEntryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryDropEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPrimaryDropEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryDropEntryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryDropEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPrimaryDropEntryAdjuster(var cap: Double) : PrimaryDropEntryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryDropEntry, value: Double): Double = minOf(value, cap)
}

data class RoundingPrimaryDropEntryAdjuster(var step: Double) : PrimaryDropEntryFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryDropEntry, value: Double): Double = roundTo(value, step)
}

data class MinimumPrimaryDropEntryAdjuster(var minimum: Double) : PrimaryDropEntryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryDropEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PrimaryDropEntryEtaMinutesOffsetAdjuster(var weight: Double) : PrimaryDropEntryFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: PrimaryDropEntry, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

/** Runs the primary drop entry steps in order and keeps a trace of each. */
data class PrimaryDropEntryEngine(
    var steps: MutableList<PrimaryDropEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryDropEntry): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryDropEntryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryDropEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryDropEntryEngine(flags: Registry): PrimaryDropEntryEngine {
    return PrimaryDropEntryEngine(
        steps = mutableListOf<PrimaryDropEntryFactor>(
            PercentPrimaryDropEntryAdjuster(9.4),
            FlaggedPrimaryDropEntryAdjuster(flags, "primary-drop-entry-strict", 1.2),
            CapPrimaryDropEntryAdjuster(33739.0),
            RoundingPrimaryDropEntryAdjuster(0.05),
            MinimumPrimaryDropEntryAdjuster(38.3),
            PrimaryDropEntryEtaMinutesOffsetAdjuster(0.79),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 83409.9,
    )
}
