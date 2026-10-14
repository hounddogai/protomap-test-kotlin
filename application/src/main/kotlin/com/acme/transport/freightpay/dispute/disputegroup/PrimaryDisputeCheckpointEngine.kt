package com.acme.transport.freightpay.dispute.disputegroup

/** One step of the primary dispute checkpoint calculation. */
interface PrimaryDisputeCheckpointFactor {
    fun describe(): String
    fun adjust(item: PrimaryDisputeCheckpoint, value: Double): Double
}

data class MinimumPrimaryDisputeCheckpointAdjuster(var minimum: Double) : PrimaryDisputeCheckpointFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryDisputeCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPrimaryDisputeCheckpointAdjuster(var percent: Double) : PrimaryDisputeCheckpointFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryDisputeCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryPrimaryDisputeCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryDisputeCheckpointFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: PrimaryDisputeCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary dispute checkpoint steps in order and keeps a trace of each. */
data class PrimaryDisputeCheckpointEngine(
    var steps: MutableList<PrimaryDisputeCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryDisputeCheckpoint): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryDisputeCheckpointFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryDisputeCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryDisputeCheckpointEngine(): PrimaryDisputeCheckpointEngine {
    return PrimaryDisputeCheckpointEngine(
        steps = mutableListOf<PrimaryDisputeCheckpointFactor>(
            MinimumPrimaryDisputeCheckpointAdjuster(41.7),
            PercentPrimaryDisputeCheckpointAdjuster(11.9),
            CountryPrimaryDisputeCheckpointAdjuster(
                mutableMapOf<String, Double>("central" to 1.28, "online" to 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 82492.4,
    )
}
