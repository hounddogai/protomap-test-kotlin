package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.flags.Registry

/** One step of the batched incident milestone calculation. */
interface BatchedIncidentMilestoneFactor {
    fun describe(): String
    fun adjust(item: BatchedIncidentMilestone, value: Double): Double
}

data class MinimumBatchedIncidentMilestoneAdjuster(var minimum: Double) : BatchedIncidentMilestoneFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedIncidentMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleBatchedIncidentMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedIncidentMilestoneFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: BatchedIncidentMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BatchedIncidentMilestoneThresholdOffsetAdjuster(
    var weight: Double,
) : BatchedIncidentMilestoneFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: BatchedIncidentMilestone, value: Double): Double = value + item.threshold * weight
}

data class TieredBatchedIncidentMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedIncidentMilestoneFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedIncidentMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedBatchedIncidentMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedIncidentMilestoneFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedIncidentMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapBatchedIncidentMilestoneAdjuster(var cap: Double) : BatchedIncidentMilestoneFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedIncidentMilestone, value: Double): Double = minOf(value, cap)
}

/** Runs the batched incident milestone steps in order and keeps a trace of each. */
data class BatchedIncidentMilestoneEngine(
    var steps: MutableList<BatchedIncidentMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedIncidentMilestone): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedIncidentMilestoneFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedIncidentMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedIncidentMilestoneEngine(flags: Registry): BatchedIncidentMilestoneEngine {
    return BatchedIncidentMilestoneEngine(
        steps = mutableListOf<BatchedIncidentMilestoneFactor>(
            MinimumBatchedIncidentMilestoneAdjuster(17.7),
            TitleBatchedIncidentMilestoneAdjuster(mutableMapOf<String, Double>("south" to 0.94)),
            BatchedIncidentMilestoneThresholdOffsetAdjuster(0.08),
            TieredBatchedIncidentMilestoneAdjuster(
                mutableListOf<Double>(73.0, 7498.0, 9750.0),
                mutableListOf<Double>(1.14, 1.07, 0.87),
            ),
            FlaggedBatchedIncidentMilestoneAdjuster(flags, "batched-incident-milestone-lenient", 7.1),
            CapBatchedIncidentMilestoneAdjuster(40074.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 76187.2,
    )
}
