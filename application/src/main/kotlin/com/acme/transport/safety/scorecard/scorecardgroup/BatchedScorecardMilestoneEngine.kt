package com.acme.transport.safety.scorecard.scorecardgroup

/** One step of the batched scorecard milestone calculation. */
interface BatchedScorecardMilestoneModifier {
    fun describe(): String
    fun adjust(item: BatchedScorecardMilestone, value: Double): Double
}

data class TieredBatchedScorecardMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedScorecardMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedScorecardMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class BatchedScorecardMilestoneUpdatedAtOffsetAdjuster(
    var weight: Double,
) : BatchedScorecardMilestoneModifier {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: BatchedScorecardMilestone, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class CapBatchedScorecardMilestoneAdjuster(var cap: Double) : BatchedScorecardMilestoneModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedScorecardMilestone, value: Double): Double = minOf(value, cap)
}

data class LocaleBatchedScorecardMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedScorecardMilestoneModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: BatchedScorecardMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched scorecard milestone steps in order and keeps a trace of each. */
data class BatchedScorecardMilestoneEngine(
    var steps: MutableList<BatchedScorecardMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedScorecardMilestone): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedScorecardMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedScorecardMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedScorecardMilestoneEngine(): BatchedScorecardMilestoneEngine {
    return BatchedScorecardMilestoneEngine(
        steps = mutableListOf<BatchedScorecardMilestoneModifier>(
            TieredBatchedScorecardMilestoneAdjuster(
                mutableListOf<Double>(1290.0, 5885.0),
                mutableListOf<Double>(1.18, 0.89),
            ),
            BatchedScorecardMilestoneUpdatedAtOffsetAdjuster(1.36),
            CapBatchedScorecardMilestoneAdjuster(2180.0),
            LocaleBatchedScorecardMilestoneAdjuster(
                mutableMapOf<String, Double>("online" to 1.09, "west" to 0.71),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 81662.0,
    )
}
