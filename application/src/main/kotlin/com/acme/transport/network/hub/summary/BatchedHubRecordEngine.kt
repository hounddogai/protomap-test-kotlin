package com.acme.transport.network.hub.summary

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched hub record calculation. */
interface BatchedHubRecordModifier {
    fun describe(): String
    fun adjust(item: BatchedHubRecord, value: Double): Double
}

data class CategoryBatchedHubRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedHubRecordModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: BatchedHubRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBatchedHubRecordAdjuster(var minimum: Double) : BatchedHubRecordModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedHubRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingBatchedHubRecordAdjuster(var step: Double) : BatchedHubRecordModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedHubRecord, value: Double): Double = roundTo(value, step)
}

data class PercentBatchedHubRecordAdjuster(var percent: Double) : BatchedHubRecordModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedHubRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched hub record steps in order and keeps a trace of each. */
data class BatchedHubRecordEngine(
    var steps: MutableList<BatchedHubRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedHubRecord): Double {
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

    fun evaluateAll(items: MutableList<BatchedHubRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedHubRecordEngine(): BatchedHubRecordEngine {
    return BatchedHubRecordEngine(
        steps = mutableListOf<BatchedHubRecordModifier>(
            CategoryBatchedHubRecordAdjuster(mutableMapOf<String, Double>("central" to 1.38, "online" to 0.87)),
            MinimumBatchedHubRecordAdjuster(35.8),
            RoundingBatchedHubRecordAdjuster(1.0),
            PercentBatchedHubRecordAdjuster(-20.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 58067.5,
    )
}
