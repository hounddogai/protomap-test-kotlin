package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.kit.flags.Registry

/** One step of the batched inspection claim calculation. */
interface BatchedInspectionClaimStep {
    fun describe(): String
    fun adjust(item: BatchedInspectionClaim, value: Double): Double
}

data class CountryBatchedInspectionClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedInspectionClaimStep {
    override fun describe(): String = "by country"

    override fun adjust(item: BatchedInspectionClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBatchedInspectionClaimAdjuster(var minimum: Double) : BatchedInspectionClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedInspectionClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBatchedInspectionClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedInspectionClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedInspectionClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BatchedInspectionClaimDistanceKmOffsetAdjuster(var weight: Double) : BatchedInspectionClaimStep {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: BatchedInspectionClaim, value: Double): Double = value + item.distanceKm * weight
}

/** Runs the batched inspection claim steps in order and keeps a trace of each. */
data class BatchedInspectionClaimEngine(
    var steps: MutableList<BatchedInspectionClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedInspectionClaim): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedInspectionClaimStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedInspectionClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedInspectionClaimEngine(flags: Registry): BatchedInspectionClaimEngine {
    return BatchedInspectionClaimEngine(
        steps = mutableListOf<BatchedInspectionClaimStep>(
            CountryBatchedInspectionClaimAdjuster(
                mutableMapOf<String, Double>("central" to 0.9, "west" to 1.31),
            ),
            MinimumBatchedInspectionClaimAdjuster(38.1),
            FlaggedBatchedInspectionClaimAdjuster(flags, "batched-inspection-claim-lenient", -5.0),
            BatchedInspectionClaimDistanceKmOffsetAdjuster(1.29),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 95412.0,
    )
}
