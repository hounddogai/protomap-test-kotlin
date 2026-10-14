package com.acme.transport.freightpay.accessorial.baseline

import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered accessorial batch calculation. */
interface TieredAccessorialBatchStep {
    fun describe(): String
    fun adjust(item: TieredAccessorialBatch, value: Double): Double
}

data class MinimumTieredAccessorialBatchAdjuster(var minimum: Double) : TieredAccessorialBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredAccessorialBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LaneTieredAccessorialBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredAccessorialBatchStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: TieredAccessorialBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTieredAccessorialBatchAdjuster(var step: Double) : TieredAccessorialBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredAccessorialBatch, value: Double): Double = roundTo(value, step)
}

data class CapTieredAccessorialBatchAdjuster(var cap: Double) : TieredAccessorialBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredAccessorialBatch, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered accessorial batch steps in order and keeps a trace of each. */
data class TieredAccessorialBatchEngine(
    var steps: MutableList<TieredAccessorialBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredAccessorialBatch): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredAccessorialBatchStep = steps[index]
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

    fun evaluateAll(items: MutableList<TieredAccessorialBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredAccessorialBatchEngine(): TieredAccessorialBatchEngine {
    return TieredAccessorialBatchEngine(
        steps = mutableListOf<TieredAccessorialBatchStep>(
            MinimumTieredAccessorialBatchAdjuster(18.8),
            LaneTieredAccessorialBatchAdjuster(mutableMapOf<String, Double>("east" to 1.36, "west" to 1.12)),
            RoundingTieredAccessorialBatchAdjuster(0.05),
            CapTieredAccessorialBatchAdjuster(12796.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 66023.6,
    )
}
