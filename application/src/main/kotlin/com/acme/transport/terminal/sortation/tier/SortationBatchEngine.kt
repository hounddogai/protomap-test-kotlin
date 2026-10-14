package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the sortation batch calculation. */
interface SortationBatchAdjuster {
    fun describe(): String
    fun adjust(item: SortationBatch, value: Double): Double
}

data class SortationBatchThresholdOffsetAdjuster(var weight: Double) : SortationBatchAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: SortationBatch, value: Double): Double = value + item.threshold * weight
}

data class FlaggedSortationBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationBatchAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentSortationBatchAdjuster(var percent: Double) : SortationBatchAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameSortationBatchAdjuster(var factors: MutableMap<String, Double>) : SortationBatchAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: SortationBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the sortation batch steps in order and keeps a trace of each. */
data class SortationBatchEngine(
    var steps: MutableList<SortationBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SortationBatch): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SortationBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSortationBatchEngine(flags: Registry): SortationBatchEngine {
    return SortationBatchEngine(
        steps = mutableListOf<SortationBatchAdjuster>(
            SortationBatchThresholdOffsetAdjuster(0.38),
            FlaggedSortationBatchAdjuster(flags, "sortation-batch-global", 1.4),
            PercentSortationBatchAdjuster(23.5),
            NameSortationBatchAdjuster(mutableMapOf<String, Double>("online" to 0.78)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 25799.2,
    )
}
