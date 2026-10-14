package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched rest break estimate calculation. */
interface BatchedRestBreakEstimateAdjuster {
    fun describe(): String
    fun adjust(item: BatchedRestBreakEstimate, value: Double): Double
}

data class ChannelBatchedRestBreakEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedRestBreakEstimateAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: BatchedRestBreakEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBatchedRestBreakEstimateAdjuster(var minimum: Double) : BatchedRestBreakEstimateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedRestBreakEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBatchedRestBreakEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedRestBreakEstimateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedRestBreakEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched rest break estimate steps in order and keeps a trace of each. */
data class BatchedRestBreakEstimateEngine(
    var steps: MutableList<BatchedRestBreakEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedRestBreakEstimate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedRestBreakEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedRestBreakEstimateEngine(flags: Registry): BatchedRestBreakEstimateEngine {
    return BatchedRestBreakEstimateEngine(
        steps = mutableListOf<BatchedRestBreakEstimateAdjuster>(
            ChannelBatchedRestBreakEstimateAdjuster(
                mutableMapOf<String, Double>("retail" to 1.0, "west" to 1.1, "central" to 1.32),
            ),
            MinimumBatchedRestBreakEstimateAdjuster(27.3),
            FlaggedBatchedRestBreakEstimateAdjuster(flags, "batched-rest-break-estimate-pending", 6.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 71155.9,
    )
}
