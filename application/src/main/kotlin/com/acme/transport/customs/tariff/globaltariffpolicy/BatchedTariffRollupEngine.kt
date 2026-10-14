package com.acme.transport.customs.tariff.globaltariffpolicy

/** One step of the batched tariff rollup calculation. */
interface BatchedTariffRollupFactor {
    fun describe(): String
    fun adjust(item: BatchedTariffRollup, value: Double): Double
}

data class BatchedTariffRollupCostOffsetAdjuster(var weight: Double) : BatchedTariffRollupFactor {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: BatchedTariffRollup, value: Double): Double = value + item.cost * weight
}

data class RegionBatchedTariffRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedTariffRollupFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: BatchedTariffRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapBatchedTariffRollupAdjuster(var cap: Double) : BatchedTariffRollupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedTariffRollup, value: Double): Double = minOf(value, cap)
}

/** Runs the batched tariff rollup steps in order and keeps a trace of each. */
data class BatchedTariffRollupEngine(
    var steps: MutableList<BatchedTariffRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedTariffRollup): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedTariffRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedTariffRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedTariffRollupEngine(): BatchedTariffRollupEngine {
    return BatchedTariffRollupEngine(
        steps = mutableListOf<BatchedTariffRollupFactor>(
            BatchedTariffRollupCostOffsetAdjuster(1.77),
            RegionBatchedTariffRollupAdjuster(mutableMapOf<String, Double>("retail" to 1.16, "online" to 1.16)),
            CapBatchedTariffRollupAdjuster(19053.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 30936.3,
    )
}
