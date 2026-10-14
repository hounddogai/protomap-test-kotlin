package com.acme.transport.safety.incident.slot

/** One step of the batched incident override calculation. */
interface BatchedIncidentOverrideStep {
    fun describe(): String
    fun adjust(item: BatchedIncidentOverride, value: Double): Double
}

data class PercentBatchedIncidentOverrideAdjuster(var percent: Double) : BatchedIncidentOverrideStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedIncidentOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapBatchedIncidentOverrideAdjuster(var cap: Double) : BatchedIncidentOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedIncidentOverride, value: Double): Double = minOf(value, cap)
}

data class MinimumBatchedIncidentOverrideAdjuster(var minimum: Double) : BatchedIncidentOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedIncidentOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredBatchedIncidentOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedIncidentOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedIncidentOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched incident override steps in order and keeps a trace of each. */
data class BatchedIncidentOverrideEngine(
    var steps: MutableList<BatchedIncidentOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedIncidentOverride): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedIncidentOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedIncidentOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedIncidentOverrideEngine(): BatchedIncidentOverrideEngine {
    return BatchedIncidentOverrideEngine(
        steps = mutableListOf<BatchedIncidentOverrideStep>(
            PercentBatchedIncidentOverrideAdjuster(-11.2),
            CapBatchedIncidentOverrideAdjuster(42948.0),
            MinimumBatchedIncidentOverrideAdjuster(48.8),
            TieredBatchedIncidentOverrideAdjuster(
                mutableListOf<Double>(1620.0, 2014.0, 8640.0),
                mutableListOf<Double>(0.92, 0.96, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 13706.4,
    )
}
