package com.acme.transport.customs.inspection.audit

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending inspection batch calculation. */
interface PendingInspectionBatchModifier {
    fun describe(): String
    fun adjust(item: PendingInspectionBatch, value: Double): Double
}

data class RoundingPendingInspectionBatchAdjuster(var step: Double) : PendingInspectionBatchModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingInspectionBatch, value: Double): Double = roundTo(value, step)
}

data class MinimumPendingInspectionBatchAdjuster(var minimum: Double) : PendingInspectionBatchModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingInspectionBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionPendingInspectionBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingInspectionBatchModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: PendingInspectionBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapPendingInspectionBatchAdjuster(var cap: Double) : PendingInspectionBatchModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingInspectionBatch, value: Double): Double = minOf(value, cap)
}

data class PendingInspectionBatchPriorityOffsetAdjuster(var weight: Double) : PendingInspectionBatchModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: PendingInspectionBatch, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class PercentPendingInspectionBatchAdjuster(var percent: Double) : PendingInspectionBatchModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingInspectionBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending inspection batch steps in order and keeps a trace of each. */
data class PendingInspectionBatchEngine(
    var steps: MutableList<PendingInspectionBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingInspectionBatch): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingInspectionBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingInspectionBatchEngine(): PendingInspectionBatchEngine {
    return PendingInspectionBatchEngine(
        steps = mutableListOf<PendingInspectionBatchModifier>(
            RoundingPendingInspectionBatchAdjuster(0.1),
            MinimumPendingInspectionBatchAdjuster(2.5),
            RegionPendingInspectionBatchAdjuster(mutableMapOf<String, Double>("north" to 1.32)),
            CapPendingInspectionBatchAdjuster(18013.0),
            PendingInspectionBatchPriorityOffsetAdjuster(1.99),
            PercentPendingInspectionBatchAdjuster(21.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 31800.9,
    )
}
