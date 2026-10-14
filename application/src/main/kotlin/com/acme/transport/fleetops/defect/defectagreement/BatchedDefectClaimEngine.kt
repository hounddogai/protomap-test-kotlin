package com.acme.transport.fleetops.defect.defectagreement

/** One step of the batched defect claim calculation. */
interface BatchedDefectClaimAdjuster {
    fun describe(): String
    fun adjust(item: BatchedDefectClaim, value: Double): Double
}

data class CapBatchedDefectClaimAdjuster(var cap: Double) : BatchedDefectClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedDefectClaim, value: Double): Double = minOf(value, cap)
}

data class TieredBatchedDefectClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedDefectClaimAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedDefectClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelBatchedDefectClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedDefectClaimAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedDefectClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentBatchedDefectClaimAdjuster(var percent: Double) : BatchedDefectClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedDefectClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched defect claim steps in order and keeps a trace of each. */
data class BatchedDefectClaimEngine(
    var steps: MutableList<BatchedDefectClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedDefectClaim): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedDefectClaimAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedDefectClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedDefectClaimEngine(): BatchedDefectClaimEngine {
    return BatchedDefectClaimEngine(
        steps = mutableListOf<BatchedDefectClaimAdjuster>(
            CapBatchedDefectClaimAdjuster(11944.0),
            TieredBatchedDefectClaimAdjuster(
                mutableListOf<Double>(3141.0, 6287.0, 6528.0),
                mutableListOf<Double>(0.91, 1.01, 0.86),
            ),
            LabelBatchedDefectClaimAdjuster(
                mutableMapOf<String, Double>("online" to 1.05, "east" to 1.11, "south" to 1.08),
            ),
            PercentBatchedDefectClaimAdjuster(2.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 97237.2,
    )
}
