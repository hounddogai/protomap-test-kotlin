package com.acme.transport.terminal.sortation.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the sortation draft calculation. */
interface SortationDraftFactor {
    fun describe(): String
    fun adjust(item: SortationDraft, value: Double): Double
}

data class CapSortationDraftAdjuster(var cap: Double) : SortationDraftFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SortationDraft, value: Double): Double = minOf(value, cap)
}

data class TieredSortationDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SortationDraftFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SortationDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumSortationDraftAdjuster(var minimum: Double) : SortationDraftFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSortationDraftAdjuster(var step: Double) : SortationDraftFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SortationDraft, value: Double): Double = roundTo(value, step)
}

data class FlaggedSortationDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationDraftFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentSortationDraftAdjuster(var percent: Double) : SortationDraftFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the sortation draft steps in order and keeps a trace of each. */
data class SortationDraftEngine(
    var steps: MutableList<SortationDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SortationDraft): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SortationDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSortationDraftEngine(flags: Registry): SortationDraftEngine {
    return SortationDraftEngine(
        steps = mutableListOf<SortationDraftFactor>(
            CapSortationDraftAdjuster(8263.0),
            TieredSortationDraftAdjuster(
                mutableListOf<Double>(48.0, 3432.0, 3816.0, 6547.0),
                mutableListOf<Double>(0.96, 1.1, 0.98, 0.86),
            ),
            MinimumSortationDraftAdjuster(26.0),
            RoundingSortationDraftAdjuster(0.25),
            FlaggedSortationDraftAdjuster(flags, "sortation-draft-global", -3.7),
            PercentSortationDraftAdjuster(18.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 97478.9,
    )
}
