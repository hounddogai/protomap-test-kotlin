package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched roster audit calculation. */
interface BatchedRosterAuditStep {
    fun describe(): String
    fun adjust(item: BatchedRosterAudit, value: Double): Double
}

data class BatchedRosterAuditWeightOffsetAdjuster(var weight: Double) : BatchedRosterAuditStep {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: BatchedRosterAudit, value: Double): Double = value + item.weight * weight
}

data class MinimumBatchedRosterAuditAdjuster(var minimum: Double) : BatchedRosterAuditStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedRosterAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingBatchedRosterAuditAdjuster(var step: Double) : BatchedRosterAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedRosterAudit, value: Double): Double = roundTo(value, step)
}

data class CapBatchedRosterAuditAdjuster(var cap: Double) : BatchedRosterAuditStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedRosterAudit, value: Double): Double = minOf(value, cap)
}

/** Runs the batched roster audit steps in order and keeps a trace of each. */
data class BatchedRosterAuditEngine(
    var steps: MutableList<BatchedRosterAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedRosterAudit): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedRosterAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedRosterAuditEngine(): BatchedRosterAuditEngine {
    return BatchedRosterAuditEngine(
        steps = mutableListOf<BatchedRosterAuditStep>(
            BatchedRosterAuditWeightOffsetAdjuster(0.43),
            MinimumBatchedRosterAuditAdjuster(28.8),
            RoundingBatchedRosterAuditAdjuster(0.25),
            CapBatchedRosterAuditAdjuster(16637.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 50727.3,
    )
}
