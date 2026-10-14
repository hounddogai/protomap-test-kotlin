package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.kit.flags.Registry

/** One step of the sortation slot calculation. */
interface SortationSlotFactor {
    fun describe(): String
    fun adjust(item: SortationSlot, value: Double): Double
}

data class LocaleSortationSlotAdjuster(var factors: MutableMap<String, Double>) : SortationSlotFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: SortationSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentSortationSlotAdjuster(var percent: Double) : SortationSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedSortationSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSortationSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SortationSlotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SortationSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the sortation slot steps in order and keeps a trace of each. */
data class SortationSlotEngine(
    var steps: MutableList<SortationSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SortationSlot): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SortationSlotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SortationSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSortationSlotEngine(flags: Registry): SortationSlotEngine {
    return SortationSlotEngine(
        steps = mutableListOf<SortationSlotFactor>(
            LocaleSortationSlotAdjuster(mutableMapOf<String, Double>("retail" to 0.8, "east" to 0.82)),
            PercentSortationSlotAdjuster(-22.6),
            FlaggedSortationSlotAdjuster(flags, "sortation-slot-adaptive", 1.4),
            TieredSortationSlotAdjuster(
                mutableListOf<Double>(3666.0, 3968.0, 7773.0, 9172.0),
                mutableListOf<Double>(0.86, 1.06, 0.87, 0.8),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 4295.0,
    )
}
