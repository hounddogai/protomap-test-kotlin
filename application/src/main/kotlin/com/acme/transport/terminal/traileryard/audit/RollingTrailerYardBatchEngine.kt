package com.acme.transport.terminal.traileryard.audit

/** One step of the rolling trailer yard batch calculation. */
interface RollingTrailerYardBatchAdjuster {
    fun describe(): String
    fun adjust(item: RollingTrailerYardBatch, value: Double): Double
}

data class MinimumRollingTrailerYardBatchAdjuster(var minimum: Double) : RollingTrailerYardBatchAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingTrailerYardBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRollingTrailerYardBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingTrailerYardBatchAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingTrailerYardBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelRollingTrailerYardBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingTrailerYardBatchAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: RollingTrailerYardBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRollingTrailerYardBatchAdjuster(var percent: Double) : RollingTrailerYardBatchAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingTrailerYardBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rolling trailer yard batch steps in order and keeps a trace of each. */
data class RollingTrailerYardBatchEngine(
    var steps: MutableList<RollingTrailerYardBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingTrailerYardBatch): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingTrailerYardBatchAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingTrailerYardBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingTrailerYardBatchEngine(): RollingTrailerYardBatchEngine {
    return RollingTrailerYardBatchEngine(
        steps = mutableListOf<RollingTrailerYardBatchAdjuster>(
            MinimumRollingTrailerYardBatchAdjuster(33.6),
            TieredRollingTrailerYardBatchAdjuster(
                mutableListOf<Double>(1865.0, 2315.0),
                mutableListOf<Double>(1.08, 0.95),
            ),
            LabelRollingTrailerYardBatchAdjuster(
                mutableMapOf<String, Double>("retail" to 1.34, "central" to 0.98, "west" to 1.38),
            ),
            PercentRollingTrailerYardBatchAdjuster(10.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 43154.1,
    )
}
