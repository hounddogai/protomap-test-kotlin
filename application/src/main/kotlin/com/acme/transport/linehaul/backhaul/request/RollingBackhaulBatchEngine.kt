package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling backhaul batch calculation. */
interface RollingBackhaulBatchAdjuster {
    fun describe(): String
    fun adjust(item: RollingBackhaulBatch, value: Double): Double
}

data class SourceRollingBackhaulBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingBackhaulBatchAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: RollingBackhaulBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRollingBackhaulBatchAdjuster(var percent: Double) : RollingBackhaulBatchAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingBackhaulBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRollingBackhaulBatchAdjuster(var step: Double) : RollingBackhaulBatchAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingBackhaulBatch, value: Double): Double = roundTo(value, step)
}

data class MinimumRollingBackhaulBatchAdjuster(var minimum: Double) : RollingBackhaulBatchAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingBackhaulBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRollingBackhaulBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingBackhaulBatchAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingBackhaulBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling backhaul batch steps in order and keeps a trace of each. */
data class RollingBackhaulBatchEngine(
    var steps: MutableList<RollingBackhaulBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingBackhaulBatch): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingBackhaulBatchAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingBackhaulBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingBackhaulBatchEngine(): RollingBackhaulBatchEngine {
    return RollingBackhaulBatchEngine(
        steps = mutableListOf<RollingBackhaulBatchAdjuster>(
            SourceRollingBackhaulBatchAdjuster(mutableMapOf<String, Double>("online" to 1.37)),
            PercentRollingBackhaulBatchAdjuster(-10.0),
            RoundingRollingBackhaulBatchAdjuster(5.0),
            MinimumRollingBackhaulBatchAdjuster(36.2),
            TieredRollingBackhaulBatchAdjuster(
                mutableListOf<Double>(6539.0, 8944.0),
                mutableListOf<Double>(1.08, 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 66428.5,
    )
}
