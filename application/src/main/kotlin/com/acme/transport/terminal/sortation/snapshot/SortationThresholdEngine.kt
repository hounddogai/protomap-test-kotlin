package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the sortation threshold calculation. */
interface SortationThresholdAdjuster {
    fun describe(): String
    fun adjust(item: SortationThreshold, value: Double): Double
}

data class FlaggedSortationThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationThresholdAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumSortationThresholdAdjuster(var minimum: Double) : SortationThresholdAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SortationThresholdRetriesOffsetAdjuster(var weight: Double) : SortationThresholdAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: SortationThreshold, value: Double): Double = value + item.retries.toDouble() * weight
}

/** Runs the sortation threshold steps in order and keeps a trace of each. */
data class SortationThresholdEngine(
    var steps: MutableList<SortationThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SortationThreshold): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SortationThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSortationThresholdEngine(flags: Registry): SortationThresholdEngine {
    return SortationThresholdEngine(
        steps = mutableListOf<SortationThresholdAdjuster>(
            FlaggedSortationThresholdAdjuster(flags, "sortation-threshold-legacy", -0.4),
            MinimumSortationThresholdAdjuster(40.1),
            SortationThresholdRetriesOffsetAdjuster(1.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 66271.0,
    )
}
