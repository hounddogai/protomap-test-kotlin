package com.acme.transport.safety.incident.incidentbatch

import com.acme.transport.kit.flags.Registry

/** One step of the weighted incident batch calculation. */
interface WeightedIncidentBatchFactor {
    fun describe(): String
    fun adjust(item: WeightedIncidentBatch, value: Double): Double
}

data class FlaggedWeightedIncidentBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedIncidentBatchFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedIncidentBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentWeightedIncidentBatchAdjuster(var percent: Double) : WeightedIncidentBatchFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedIncidentBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelWeightedIncidentBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedIncidentBatchFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: WeightedIncidentBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumWeightedIncidentBatchAdjuster(var minimum: Double) : WeightedIncidentBatchFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedIncidentBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapWeightedIncidentBatchAdjuster(var cap: Double) : WeightedIncidentBatchFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedIncidentBatch, value: Double): Double = minOf(value, cap)
}

/** Runs the weighted incident batch steps in order and keeps a trace of each. */
data class WeightedIncidentBatchEngine(
    var steps: MutableList<WeightedIncidentBatchFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedIncidentBatch): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedIncidentBatchFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedIncidentBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedIncidentBatchEngine(flags: Registry): WeightedIncidentBatchEngine {
    return WeightedIncidentBatchEngine(
        steps = mutableListOf<WeightedIncidentBatchFactor>(
            FlaggedWeightedIncidentBatchAdjuster(flags, "weighted-incident-batch-weighted", -2.3),
            PercentWeightedIncidentBatchAdjuster(16.4),
            LabelWeightedIncidentBatchAdjuster(mutableMapOf<String, Double>("online" to 1.14)),
            MinimumWeightedIncidentBatchAdjuster(32.6),
            CapWeightedIncidentBatchAdjuster(40590.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 45009.4,
    )
}
