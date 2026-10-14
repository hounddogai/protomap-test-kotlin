package com.acme.transport.freightpay.audit.plan

/** One step of the batched audit milestone calculation. */
interface BatchedAuditMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: BatchedAuditMilestone, value: Double): Double
}

data class TieredBatchedAuditMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedAuditMilestoneAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedAuditMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentBatchedAuditMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedAuditMilestoneAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: BatchedAuditMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBatchedAuditMilestoneAdjuster(var minimum: Double) : BatchedAuditMilestoneAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedAuditMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBatchedAuditMilestoneAdjuster(var percent: Double) : BatchedAuditMilestoneAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedAuditMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched audit milestone steps in order and keeps a trace of each. */
data class BatchedAuditMilestoneEngine(
    var steps: MutableList<BatchedAuditMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedAuditMilestone): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedAuditMilestoneAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedAuditMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedAuditMilestoneEngine(): BatchedAuditMilestoneEngine {
    return BatchedAuditMilestoneEngine(
        steps = mutableListOf<BatchedAuditMilestoneAdjuster>(
            TieredBatchedAuditMilestoneAdjuster(
                mutableListOf<Double>(1682.0, 3365.0, 6022.0),
                mutableListOf<Double>(0.99, 1.17, 0.87),
            ),
            SegmentBatchedAuditMilestoneAdjuster(mutableMapOf<String, Double>("south" to 1.35)),
            MinimumBatchedAuditMilestoneAdjuster(21.8),
            PercentBatchedAuditMilestoneAdjuster(6.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 33350.8,
    )
}
