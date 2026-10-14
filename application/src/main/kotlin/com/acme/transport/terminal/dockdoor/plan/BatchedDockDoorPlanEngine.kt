package com.acme.transport.terminal.dockdoor.plan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched dock door plan calculation. */
interface BatchedDockDoorPlanAdjuster {
    fun describe(): String
    fun adjust(item: BatchedDockDoorPlan, value: Double): Double
}

data class RoundingBatchedDockDoorPlanAdjuster(var step: Double) : BatchedDockDoorPlanAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedDockDoorPlan, value: Double): Double = roundTo(value, step)
}

data class BatchedDockDoorPlanRetriesOffsetAdjuster(var weight: Double) : BatchedDockDoorPlanAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: BatchedDockDoorPlan, value: Double): Double = value + item.retries.toDouble() * weight
}

data class PercentBatchedDockDoorPlanAdjuster(var percent: Double) : BatchedDockDoorPlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedDockDoorPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched dock door plan steps in order and keeps a trace of each. */
data class BatchedDockDoorPlanEngine(
    var steps: MutableList<BatchedDockDoorPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedDockDoorPlan): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedDockDoorPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedDockDoorPlanEngine(): BatchedDockDoorPlanEngine {
    return BatchedDockDoorPlanEngine(
        steps = mutableListOf<BatchedDockDoorPlanAdjuster>(
            RoundingBatchedDockDoorPlanAdjuster(0.5),
            BatchedDockDoorPlanRetriesOffsetAdjuster(0.71),
            PercentBatchedDockDoorPlanAdjuster(-24.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 89762.6,
    )
}
