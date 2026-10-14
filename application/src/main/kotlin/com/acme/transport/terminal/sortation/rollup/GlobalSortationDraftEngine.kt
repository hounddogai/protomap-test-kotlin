package com.acme.transport.terminal.sortation.rollup

import com.acme.transport.kit.numbers.roundTo

/** One step of the global sortation draft calculation. */
interface GlobalSortationDraftAdjuster {
    fun describe(): String
    fun adjust(item: GlobalSortationDraft, value: Double): Double
}

data class MinimumGlobalSortationDraftAdjuster(var minimum: Double) : GlobalSortationDraftAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalSortationDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class GlobalSortationDraftDistanceKmOffsetAdjuster(var weight: Double) : GlobalSortationDraftAdjuster {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: GlobalSortationDraft, value: Double): Double = value + item.distanceKm * weight
}

data class SegmentGlobalSortationDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalSortationDraftAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: GlobalSortationDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingGlobalSortationDraftAdjuster(var step: Double) : GlobalSortationDraftAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalSortationDraft, value: Double): Double = roundTo(value, step)
}

data class PercentGlobalSortationDraftAdjuster(var percent: Double) : GlobalSortationDraftAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalSortationDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredGlobalSortationDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalSortationDraftAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalSortationDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global sortation draft steps in order and keeps a trace of each. */
data class GlobalSortationDraftEngine(
    var steps: MutableList<GlobalSortationDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalSortationDraft): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalSortationDraftAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalSortationDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalSortationDraftEngine(): GlobalSortationDraftEngine {
    return GlobalSortationDraftEngine(
        steps = mutableListOf<GlobalSortationDraftAdjuster>(
            MinimumGlobalSortationDraftAdjuster(34.2),
            GlobalSortationDraftDistanceKmOffsetAdjuster(0.42),
            SegmentGlobalSortationDraftAdjuster(mutableMapOf<String, Double>("online" to 1.29)),
            RoundingGlobalSortationDraftAdjuster(0.1),
            PercentGlobalSortationDraftAdjuster(18.9),
            TieredGlobalSortationDraftAdjuster(
                mutableListOf<Double>(2506.0, 4256.0, 5116.0, 5916.0),
                mutableListOf<Double>(1.05, 1.12, 1.11, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 62903.4,
    )
}
