package com.acme.transport.terminal.sortation.digest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the sortation digest calculation. */
interface SortationDigestAdjuster {
    fun describe(): String
    fun adjust(item: SortationDigest, value: Double): Double
}

data class CountrySortationDigestAdjuster(var factors: MutableMap<String, Double>) : SortationDigestAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: SortationDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class SortationDigestCostOffsetAdjuster(var weight: Double) : SortationDigestAdjuster {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: SortationDigest, value: Double): Double = value + item.cost * weight
}

data class PercentSortationDigestAdjuster(var percent: Double) : SortationDigestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumSortationDigestAdjuster(var minimum: Double) : SortationDigestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the sortation digest steps in order and keeps a trace of each. */
data class SortationDigestEngine(
    var steps: MutableList<SortationDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SortationDigest): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SortationDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSortationDigestEngine(): SortationDigestEngine {
    return SortationDigestEngine(
        steps = mutableListOf<SortationDigestAdjuster>(
            CountrySortationDigestAdjuster(mutableMapOf<String, Double>("retail" to 0.98, "central" to 0.89)),
            SortationDigestCostOffsetAdjuster(1.63),
            PercentSortationDigestAdjuster(14.4),
            MinimumSortationDigestAdjuster(26.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 51302.5,
    )
}
