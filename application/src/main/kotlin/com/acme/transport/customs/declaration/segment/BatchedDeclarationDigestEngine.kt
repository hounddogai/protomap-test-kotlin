package com.acme.transport.customs.declaration.segment

/** One step of the batched declaration digest calculation. */
interface BatchedDeclarationDigestStep {
    fun describe(): String
    fun adjust(item: BatchedDeclarationDigest, value: Double): Double
}

data class PercentBatchedDeclarationDigestAdjuster(var percent: Double) : BatchedDeclarationDigestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedDeclarationDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredBatchedDeclarationDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedDeclarationDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedDeclarationDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class BatchedDeclarationDigestMarginOffsetAdjuster(var weight: Double) : BatchedDeclarationDigestStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: BatchedDeclarationDigest, value: Double): Double = value + item.margin * weight
}

/** Runs the batched declaration digest steps in order and keeps a trace of each. */
data class BatchedDeclarationDigestEngine(
    var steps: MutableList<BatchedDeclarationDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedDeclarationDigest): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedDeclarationDigestStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedDeclarationDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedDeclarationDigestEngine(): BatchedDeclarationDigestEngine {
    return BatchedDeclarationDigestEngine(
        steps = mutableListOf<BatchedDeclarationDigestStep>(
            PercentBatchedDeclarationDigestAdjuster(-5.2),
            TieredBatchedDeclarationDigestAdjuster(
                mutableListOf<Double>(910.0, 1876.0, 9629.0),
                mutableListOf<Double>(1.16, 1.19, 1.09),
            ),
            BatchedDeclarationDigestMarginOffsetAdjuster(0.77),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 2372.2,
    )
}
