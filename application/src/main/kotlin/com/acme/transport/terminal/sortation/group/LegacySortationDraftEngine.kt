package com.acme.transport.terminal.sortation.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy sortation draft calculation. */
interface LegacySortationDraftAdjuster {
    fun describe(): String
    fun adjust(item: LegacySortationDraft, value: Double): Double
}

data class LegacySortationDraftPriorityOffsetAdjuster(var weight: Double) : LegacySortationDraftAdjuster {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: LegacySortationDraft, value: Double): Double = value + item.priority.toDouble() * weight
}

data class CapLegacySortationDraftAdjuster(var cap: Double) : LegacySortationDraftAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacySortationDraft, value: Double): Double = minOf(value, cap)
}

data class PercentLegacySortationDraftAdjuster(var percent: Double) : LegacySortationDraftAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacySortationDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryLegacySortationDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacySortationDraftAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: LegacySortationDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLegacySortationDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacySortationDraftAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacySortationDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy sortation draft steps in order and keeps a trace of each. */
data class LegacySortationDraftEngine(
    var steps: MutableList<LegacySortationDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacySortationDraft): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacySortationDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacySortationDraftEngine(flags: Registry): LegacySortationDraftEngine {
    return LegacySortationDraftEngine(
        steps = mutableListOf<LegacySortationDraftAdjuster>(
            LegacySortationDraftPriorityOffsetAdjuster(0.22),
            CapLegacySortationDraftAdjuster(23171.0),
            PercentLegacySortationDraftAdjuster(2.7),
            CountryLegacySortationDraftAdjuster(
                mutableMapOf<String, Double>("north" to 1.09, "east" to 0.91, "south" to 1.15),
            ),
            FlaggedLegacySortationDraftAdjuster(flags, "legacy-sortation-draft-strict", 4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 56752.3,
    )
}
