package com.acme.transport.safety.nearmiss.cycle

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched near miss variance calculation. */
interface BatchedNearMissVarianceStep {
    fun describe(): String
    fun adjust(item: BatchedNearMissVariance, value: Double): Double
}

data class RoundingBatchedNearMissVarianceAdjuster(var step: Double) : BatchedNearMissVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedNearMissVariance, value: Double): Double = roundTo(value, step)
}

data class PercentBatchedNearMissVarianceAdjuster(var percent: Double) : BatchedNearMissVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedNearMissVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionBatchedNearMissVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedNearMissVarianceStep {
    override fun describe(): String = "by region"

    override fun adjust(item: BatchedNearMissVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapBatchedNearMissVarianceAdjuster(var cap: Double) : BatchedNearMissVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedNearMissVariance, value: Double): Double = minOf(value, cap)
}

data class TieredBatchedNearMissVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedNearMissVarianceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedNearMissVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched near miss variance steps in order and keeps a trace of each. */
data class BatchedNearMissVarianceEngine(
    var steps: MutableList<BatchedNearMissVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedNearMissVariance): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedNearMissVarianceStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedNearMissVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedNearMissVarianceEngine(): BatchedNearMissVarianceEngine {
    return BatchedNearMissVarianceEngine(
        steps = mutableListOf<BatchedNearMissVarianceStep>(
            RoundingBatchedNearMissVarianceAdjuster(0.25),
            PercentBatchedNearMissVarianceAdjuster(3.2),
            RegionBatchedNearMissVarianceAdjuster(mutableMapOf<String, Double>("north" to 1.04, "east" to 1.02)),
            CapBatchedNearMissVarianceAdjuster(15304.0),
            TieredBatchedNearMissVarianceAdjuster(
                mutableListOf<Double>(195.0, 1098.0, 1540.0, 8937.0),
                mutableListOf<Double>(0.85, 0.98, 0.93, 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 72917.7,
    )
}
