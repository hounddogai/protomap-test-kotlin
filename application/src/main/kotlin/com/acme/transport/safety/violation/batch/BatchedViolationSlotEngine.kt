package com.acme.transport.safety.violation.batch

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched violation slot calculation. */
interface BatchedViolationSlotModifier {
    fun describe(): String
    fun adjust(item: BatchedViolationSlot, value: Double): Double
}

data class RoundingBatchedViolationSlotAdjuster(var step: Double) : BatchedViolationSlotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedViolationSlot, value: Double): Double = roundTo(value, step)
}

data class MinimumBatchedViolationSlotAdjuster(var minimum: Double) : BatchedViolationSlotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedViolationSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BatchedViolationSlotAmountOffsetAdjuster(var weight: Double) : BatchedViolationSlotModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: BatchedViolationSlot, value: Double): Double = value + item.amount * weight
}

data class CapBatchedViolationSlotAdjuster(var cap: Double) : BatchedViolationSlotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedViolationSlot, value: Double): Double = minOf(value, cap)
}

data class LabelBatchedViolationSlotAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedViolationSlotModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedViolationSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched violation slot steps in order and keeps a trace of each. */
data class BatchedViolationSlotEngine(
    var steps: MutableList<BatchedViolationSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedViolationSlot): Double {
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

    fun evaluateAll(items: MutableList<BatchedViolationSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedViolationSlotEngine(): BatchedViolationSlotEngine {
    return BatchedViolationSlotEngine(
        steps = mutableListOf<BatchedViolationSlotModifier>(
            RoundingBatchedViolationSlotAdjuster(0.25),
            MinimumBatchedViolationSlotAdjuster(33.3),
            BatchedViolationSlotAmountOffsetAdjuster(1.65),
            CapBatchedViolationSlotAdjuster(8073.0),
            LabelBatchedViolationSlotAdjuster(mutableMapOf<String, Double>("north" to 0.71, "south" to 0.75)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 48334.9,
    )
}
