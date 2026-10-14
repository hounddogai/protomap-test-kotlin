package com.acme.transport.linehaul.trailer.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer batch calculation. */
interface TrailerBatchAdjuster {
    fun describe(): String
    fun adjust(item: TrailerBatch, value: Double): Double
}

data class FlaggedTrailerBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerBatchAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TrailerBatchCreatedAtOffsetAdjuster(var weight: Double) : TrailerBatchAdjuster {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: TrailerBatch, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class LabelTrailerBatchAdjuster(var factors: MutableMap<String, Double>) : TrailerBatchAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: TrailerBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTrailerBatchAdjuster(var step: Double) : TrailerBatchAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerBatch, value: Double): Double = roundTo(value, step)
}

/** Runs the trailer batch steps in order and keeps a trace of each. */
data class TrailerBatchEngine(
    var steps: MutableList<TrailerBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerBatch): Double {
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

    fun evaluateAll(items: MutableList<TrailerBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerBatchEngine(flags: Registry): TrailerBatchEngine {
    return TrailerBatchEngine(
        steps = mutableListOf<TrailerBatchAdjuster>(
            FlaggedTrailerBatchAdjuster(flags, "trailer-batch-weighted", 8.0),
            TrailerBatchCreatedAtOffsetAdjuster(1.17),
            LabelTrailerBatchAdjuster(
                mutableMapOf<String, Double>("north" to 1.12, "online" to 1.33, "east" to 1.25),
            ),
            RoundingTrailerBatchAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 32402.2,
    )
}
