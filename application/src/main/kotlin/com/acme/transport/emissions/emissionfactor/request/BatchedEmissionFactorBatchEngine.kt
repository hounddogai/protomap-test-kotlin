package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.kit.flags.Registry

/** One step of the batched emission factor batch calculation. */
interface BatchedEmissionFactorBatchModifier {
    fun describe(): String
    fun adjust(item: BatchedEmissionFactorBatch, value: Double): Double
}

data class BatchedEmissionFactorBatchLimitOffsetAdjuster(
    var weight: Double,
) : BatchedEmissionFactorBatchModifier {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: BatchedEmissionFactorBatch, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

data class TieredBatchedEmissionFactorBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedEmissionFactorBatchModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedEmissionFactorBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotBatchedEmissionFactorBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedEmissionFactorBatchModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: BatchedEmissionFactorBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBatchedEmissionFactorBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedEmissionFactorBatchModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedEmissionFactorBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched emission factor batch steps in order and keeps a trace of each. */
data class BatchedEmissionFactorBatchEngine(
    var steps: MutableList<BatchedEmissionFactorBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedEmissionFactorBatch): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedEmissionFactorBatchModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedEmissionFactorBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedEmissionFactorBatchEngine(flags: Registry): BatchedEmissionFactorBatchEngine {
    return BatchedEmissionFactorBatchEngine(
        steps = mutableListOf<BatchedEmissionFactorBatchModifier>(
            BatchedEmissionFactorBatchLimitOffsetAdjuster(0.49),
            TieredBatchedEmissionFactorBatchAdjuster(
                mutableListOf<Double>(5630.0, 8142.0, 9361.0, 9572.0),
                mutableListOf<Double>(0.9, 0.95, 0.83, 0.84),
            ),
            DepotBatchedEmissionFactorBatchAdjuster(
                mutableMapOf<String, Double>("north" to 0.82, "south" to 1.36),
            ),
            FlaggedBatchedEmissionFactorBatchAdjuster(flags, "batched-emission-factor-batch-capped", 5.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 22259.2,
    )
}
