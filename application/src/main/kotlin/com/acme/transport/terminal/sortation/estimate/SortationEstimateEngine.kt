package com.acme.transport.terminal.sortation.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the sortation estimate calculation. */
interface SortationEstimateModifier {
    fun describe(): String
    fun adjust(item: SortationEstimate, value: Double): Double
}

data class PercentSortationEstimateAdjuster(var percent: Double) : SortationEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumSortationEstimateAdjuster(var minimum: Double) : SortationEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotSortationEstimateAdjuster(var factors: MutableMap<String, Double>) : SortationEstimateModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: SortationEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedSortationEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the sortation estimate steps in order and keeps a trace of each. */
data class SortationEstimateEngine(
    var steps: MutableList<SortationEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SortationEstimate): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SortationEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSortationEstimateEngine(flags: Registry): SortationEstimateEngine {
    return SortationEstimateEngine(
        steps = mutableListOf<SortationEstimateModifier>(
            PercentSortationEstimateAdjuster(16.6),
            MinimumSortationEstimateAdjuster(33.4),
            DepotSortationEstimateAdjuster(mutableMapOf<String, Double>("west" to 1.25, "retail" to 0.85)),
            FlaggedSortationEstimateAdjuster(flags, "sortation-estimate-regional", 2.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 17812.8,
    )
}
