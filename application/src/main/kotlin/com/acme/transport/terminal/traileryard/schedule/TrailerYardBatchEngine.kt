package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer yard batch calculation. */
interface TrailerYardBatchAdjuster {
    fun describe(): String
    fun adjust(item: TrailerYardBatch, value: Double): Double
}

data class PercentTrailerYardBatchAdjuster(var percent: Double) : TrailerYardBatchAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerYardBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameTrailerYardBatchAdjuster(var factors: MutableMap<String, Double>) : TrailerYardBatchAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: TrailerYardBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTrailerYardBatchAdjuster(var minimum: Double) : TrailerYardBatchAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerYardBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the trailer yard batch steps in order and keeps a trace of each. */
data class TrailerYardBatchEngine(
    var steps: MutableList<TrailerYardBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerYardBatch): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerYardBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerYardBatchEngine(): TrailerYardBatchEngine {
    return TrailerYardBatchEngine(
        steps = mutableListOf<TrailerYardBatchAdjuster>(
            PercentTrailerYardBatchAdjuster(9.7),
            NameTrailerYardBatchAdjuster(mutableMapOf<String, Double>("retail" to 1.11)),
            MinimumTrailerYardBatchAdjuster(21.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 54359.4,
    )
}
