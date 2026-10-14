package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted sortation override calculation. */
interface WeightedSortationOverrideAdjuster {
    fun describe(): String
    fun adjust(item: WeightedSortationOverride, value: Double): Double
}

data class WeightedSortationOverrideMarginOffsetAdjuster(
    var weight: Double,
) : WeightedSortationOverrideAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: WeightedSortationOverride, value: Double): Double = value + item.margin * weight
}

data class MinimumWeightedSortationOverrideAdjuster(var minimum: Double) : WeightedSortationOverrideAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedSortationOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapWeightedSortationOverrideAdjuster(var cap: Double) : WeightedSortationOverrideAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedSortationOverride, value: Double): Double = minOf(value, cap)
}

data class FlaggedWeightedSortationOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedSortationOverrideAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedSortationOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentWeightedSortationOverrideAdjuster(var percent: Double) : WeightedSortationOverrideAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedSortationOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameWeightedSortationOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedSortationOverrideAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: WeightedSortationOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted sortation override steps in order and keeps a trace of each. */
data class WeightedSortationOverrideEngine(
    var steps: MutableList<WeightedSortationOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedSortationOverride): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedSortationOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedSortationOverrideEngine(flags: Registry): WeightedSortationOverrideEngine {
    return WeightedSortationOverrideEngine(
        steps = mutableListOf<WeightedSortationOverrideAdjuster>(
            WeightedSortationOverrideMarginOffsetAdjuster(0.19),
            MinimumWeightedSortationOverrideAdjuster(36.1),
            CapWeightedSortationOverrideAdjuster(28821.0),
            FlaggedWeightedSortationOverrideAdjuster(flags, "weighted-sortation-override-global", 4.4),
            PercentWeightedSortationOverrideAdjuster(-15.4),
            NameWeightedSortationOverrideAdjuster(
                mutableMapOf<String, Double>("east" to 0.96, "west" to 1.07, "south" to 1.33),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 74704.2,
    )
}
