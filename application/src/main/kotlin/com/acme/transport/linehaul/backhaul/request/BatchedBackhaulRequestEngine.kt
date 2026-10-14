package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched backhaul request calculation. */
interface BatchedBackhaulRequestStep {
    fun describe(): String
    fun adjust(item: BatchedBackhaulRequest, value: Double): Double
}

data class RoundingBatchedBackhaulRequestAdjuster(var step: Double) : BatchedBackhaulRequestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedBackhaulRequest, value: Double): Double = roundTo(value, step)
}

data class MinimumBatchedBackhaulRequestAdjuster(var minimum: Double) : BatchedBackhaulRequestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedBackhaulRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapBatchedBackhaulRequestAdjuster(var cap: Double) : BatchedBackhaulRequestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedBackhaulRequest, value: Double): Double = minOf(value, cap)
}

data class FlaggedBatchedBackhaulRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedBackhaulRequestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedBackhaulRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BatchedBackhaulRequestOdometerOffsetAdjuster(var weight: Double) : BatchedBackhaulRequestStep {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: BatchedBackhaulRequest, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class PercentBatchedBackhaulRequestAdjuster(var percent: Double) : BatchedBackhaulRequestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedBackhaulRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched backhaul request steps in order and keeps a trace of each. */
data class BatchedBackhaulRequestEngine(
    var steps: MutableList<BatchedBackhaulRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedBackhaulRequest): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedBackhaulRequestStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedBackhaulRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedBackhaulRequestEngine(flags: Registry): BatchedBackhaulRequestEngine {
    return BatchedBackhaulRequestEngine(
        steps = mutableListOf<BatchedBackhaulRequestStep>(
            RoundingBatchedBackhaulRequestAdjuster(0.1),
            MinimumBatchedBackhaulRequestAdjuster(31.0),
            CapBatchedBackhaulRequestAdjuster(35654.0),
            FlaggedBatchedBackhaulRequestAdjuster(flags, "batched-backhaul-request-capped", 7.8),
            BatchedBackhaulRequestOdometerOffsetAdjuster(1.26),
            PercentBatchedBackhaulRequestAdjuster(-6.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 18272.8,
    )
}
