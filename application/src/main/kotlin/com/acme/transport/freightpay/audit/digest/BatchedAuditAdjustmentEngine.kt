package com.acme.transport.freightpay.audit.digest

/** One step of the batched audit adjustment calculation. */
interface BatchedAuditAdjustmentStep {
    fun describe(): String
    fun adjust(item: BatchedAuditAdjustment, value: Double): Double
}

data class MinimumBatchedAuditAdjustmentAdjuster(var minimum: Double) : BatchedAuditAdjustmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedAuditAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BatchedAuditAdjustmentAttemptsOffsetAdjuster(var weight: Double) : BatchedAuditAdjustmentStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: BatchedAuditAdjustment, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class PercentBatchedAuditAdjustmentAdjuster(var percent: Double) : BatchedAuditAdjustmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedAuditAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapBatchedAuditAdjustmentAdjuster(var cap: Double) : BatchedAuditAdjustmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedAuditAdjustment, value: Double): Double = minOf(value, cap)
}

/** Runs the batched audit adjustment steps in order and keeps a trace of each. */
data class BatchedAuditAdjustmentEngine(
    var steps: MutableList<BatchedAuditAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedAuditAdjustment): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedAuditAdjustmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedAuditAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedAuditAdjustmentEngine(): BatchedAuditAdjustmentEngine {
    return BatchedAuditAdjustmentEngine(
        steps = mutableListOf<BatchedAuditAdjustmentStep>(
            MinimumBatchedAuditAdjustmentAdjuster(6.5),
            BatchedAuditAdjustmentAttemptsOffsetAdjuster(0.15),
            PercentBatchedAuditAdjustmentAdjuster(24.8),
            CapBatchedAuditAdjustmentAdjuster(22248.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 62182.6,
    )
}
