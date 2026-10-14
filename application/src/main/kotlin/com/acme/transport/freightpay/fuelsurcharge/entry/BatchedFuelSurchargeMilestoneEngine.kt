package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched fuel surcharge milestone calculation. */
interface BatchedFuelSurchargeMilestoneStep {
    fun describe(): String
    fun adjust(item: BatchedFuelSurchargeMilestone, value: Double): Double
}

data class CapBatchedFuelSurchargeMilestoneAdjuster(var cap: Double) : BatchedFuelSurchargeMilestoneStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedFuelSurchargeMilestone, value: Double): Double = minOf(value, cap)
}

data class BatchedFuelSurchargeMilestoneWeightOffsetAdjuster(
    var weight: Double,
) : BatchedFuelSurchargeMilestoneStep {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: BatchedFuelSurchargeMilestone, value: Double): Double = value + item.weight * weight
}

data class FlaggedBatchedFuelSurchargeMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedFuelSurchargeMilestoneStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedFuelSurchargeMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched fuel surcharge milestone steps in order and keeps a trace of each. */
data class BatchedFuelSurchargeMilestoneEngine(
    var steps: MutableList<BatchedFuelSurchargeMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedFuelSurchargeMilestone): Double {
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

    fun evaluateAll(items: MutableList<BatchedFuelSurchargeMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedFuelSurchargeMilestoneEngine(flags: Registry): BatchedFuelSurchargeMilestoneEngine {
    return BatchedFuelSurchargeMilestoneEngine(
        steps = mutableListOf<BatchedFuelSurchargeMilestoneStep>(
            CapBatchedFuelSurchargeMilestoneAdjuster(17264.0),
            BatchedFuelSurchargeMilestoneWeightOffsetAdjuster(1.8),
            FlaggedBatchedFuelSurchargeMilestoneAdjuster(flags, "batched-fuel-surcharge-milestone-lenient", 9.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 40542.0,
    )
}
