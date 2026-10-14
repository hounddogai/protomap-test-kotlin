package com.acme.transport.fleetops.tire.baseline

/** One step of the pending tire metric calculation. */
interface PendingTireMetricStep {
    fun describe(): String
    fun adjust(item: PendingTireMetric, value: Double): Double
}

data class TieredPendingTireMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingTireMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingTireMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NamePendingTireMetricAdjuster(var factors: MutableMap<String, Double>) : PendingTireMetricStep {
    override fun describe(): String = "by name"

    override fun adjust(item: PendingTireMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumPendingTireMetricAdjuster(var minimum: Double) : PendingTireMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingTireMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPendingTireMetricAdjuster(var cap: Double) : PendingTireMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingTireMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the pending tire metric steps in order and keeps a trace of each. */
data class PendingTireMetricEngine(
    var steps: MutableList<PendingTireMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingTireMetric): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingTireMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingTireMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingTireMetricEngine(): PendingTireMetricEngine {
    return PendingTireMetricEngine(
        steps = mutableListOf<PendingTireMetricStep>(
            TieredPendingTireMetricAdjuster(
                mutableListOf<Double>(2231.0, 3428.0),
                mutableListOf<Double>(1.06, 1.18),
            ),
            NamePendingTireMetricAdjuster(mutableMapOf<String, Double>("online" to 0.89, "central" to 0.91)),
            MinimumPendingTireMetricAdjuster(18.5),
            CapPendingTireMetricAdjuster(38387.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 1688.1,
    )
}
