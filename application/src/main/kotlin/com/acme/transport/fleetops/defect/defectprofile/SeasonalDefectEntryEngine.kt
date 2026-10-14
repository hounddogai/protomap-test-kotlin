package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal defect entry calculation. */
interface SeasonalDefectEntryStep {
    fun describe(): String
    fun adjust(item: SeasonalDefectEntry, value: Double): Double
}

data class CapSeasonalDefectEntryAdjuster(var cap: Double) : SeasonalDefectEntryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalDefectEntry, value: Double): Double = minOf(value, cap)
}

data class PercentSeasonalDefectEntryAdjuster(var percent: Double) : SeasonalDefectEntryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalDefectEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumSeasonalDefectEntryAdjuster(var minimum: Double) : SeasonalDefectEntryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalDefectEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSeasonalDefectEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalDefectEntryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalDefectEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal defect entry steps in order and keeps a trace of each. */
data class SeasonalDefectEntryEngine(
    var steps: MutableList<SeasonalDefectEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalDefectEntry): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalDefectEntryStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalDefectEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalDefectEntryEngine(flags: Registry): SeasonalDefectEntryEngine {
    return SeasonalDefectEntryEngine(
        steps = mutableListOf<SeasonalDefectEntryStep>(
            CapSeasonalDefectEntryAdjuster(29832.0),
            PercentSeasonalDefectEntryAdjuster(-23.7),
            MinimumSeasonalDefectEntryAdjuster(23.4),
            FlaggedSeasonalDefectEntryAdjuster(flags, "seasonal-defect-entry-scheduled", -5.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 91776.2,
    )
}
