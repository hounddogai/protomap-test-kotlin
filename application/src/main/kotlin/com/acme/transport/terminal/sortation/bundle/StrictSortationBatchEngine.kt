package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict sortation batch calculation. */
interface StrictSortationBatchStep {
    fun describe(): String
    fun adjust(item: StrictSortationBatch, value: Double): Double
}

data class RoundingStrictSortationBatchAdjuster(var step: Double) : StrictSortationBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictSortationBatch, value: Double): Double = roundTo(value, step)
}

data class PercentStrictSortationBatchAdjuster(var percent: Double) : StrictSortationBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictSortationBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class StrictSortationBatchScoreOffsetAdjuster(var weight: Double) : StrictSortationBatchStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: StrictSortationBatch, value: Double): Double = value + item.score * weight
}

data class CapStrictSortationBatchAdjuster(var cap: Double) : StrictSortationBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictSortationBatch, value: Double): Double = minOf(value, cap)
}

data class FlaggedStrictSortationBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictSortationBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictSortationBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleStrictSortationBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictSortationBatchStep {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictSortationBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict sortation batch steps in order and keeps a trace of each. */
data class StrictSortationBatchEngine(
    var steps: MutableList<StrictSortationBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictSortationBatch): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictSortationBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictSortationBatchEngine(flags: Registry): StrictSortationBatchEngine {
    return StrictSortationBatchEngine(
        steps = mutableListOf<StrictSortationBatchStep>(
            RoundingStrictSortationBatchAdjuster(0.5),
            PercentStrictSortationBatchAdjuster(-21.3),
            StrictSortationBatchScoreOffsetAdjuster(1.63),
            CapStrictSortationBatchAdjuster(30696.0),
            FlaggedStrictSortationBatchAdjuster(flags, "strict-sortation-batch-batched", 6.3),
            TitleStrictSortationBatchAdjuster(
                mutableMapOf<String, Double>("west" to 1.34, "north" to 0.71, "south" to 1.26),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 25255.2,
    )
}
