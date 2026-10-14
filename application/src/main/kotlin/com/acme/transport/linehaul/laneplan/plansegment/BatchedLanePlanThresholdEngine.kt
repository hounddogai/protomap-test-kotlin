package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched lane plan threshold calculation. */
interface BatchedLanePlanThresholdModifier {
    fun describe(): String
    fun adjust(item: BatchedLanePlanThreshold, value: Double): Double
}

data class BatchedLanePlanThresholdPalletsOffsetAdjuster(
    var weight: Double,
) : BatchedLanePlanThresholdModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: BatchedLanePlanThreshold, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class FlaggedBatchedLanePlanThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedLanePlanThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedLanePlanThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBatchedLanePlanThresholdAdjuster(var minimum: Double) : BatchedLanePlanThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedLanePlanThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched lane plan threshold steps in order and keeps a trace of each. */
data class BatchedLanePlanThresholdEngine(
    var steps: MutableList<BatchedLanePlanThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedLanePlanThreshold): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedLanePlanThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedLanePlanThresholdEngine(flags: Registry): BatchedLanePlanThresholdEngine {
    return BatchedLanePlanThresholdEngine(
        steps = mutableListOf<BatchedLanePlanThresholdModifier>(
            BatchedLanePlanThresholdPalletsOffsetAdjuster(0.45),
            FlaggedBatchedLanePlanThresholdAdjuster(flags, "batched-lane-plan-threshold-batched", -7.3),
            MinimumBatchedLanePlanThresholdAdjuster(27.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 36289.1,
    )
}
