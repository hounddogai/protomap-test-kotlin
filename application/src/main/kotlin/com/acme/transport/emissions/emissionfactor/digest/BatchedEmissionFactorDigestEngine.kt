package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched emission factor digest calculation. */
interface BatchedEmissionFactorDigestModifier {
    fun describe(): String
    fun adjust(item: BatchedEmissionFactorDigest, value: Double): Double
}

data class MinimumBatchedEmissionFactorDigestAdjuster(
    var minimum: Double,
) : BatchedEmissionFactorDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedEmissionFactorDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBatchedEmissionFactorDigestAdjuster(
    var percent: Double,
) : BatchedEmissionFactorDigestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedEmissionFactorDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredBatchedEmissionFactorDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedEmissionFactorDigestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedEmissionFactorDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched emission factor digest steps in order and keeps a trace of each. */
data class BatchedEmissionFactorDigestEngine(
    var steps: MutableList<BatchedEmissionFactorDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedEmissionFactorDigest): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedEmissionFactorDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedEmissionFactorDigestEngine(): BatchedEmissionFactorDigestEngine {
    return BatchedEmissionFactorDigestEngine(
        steps = mutableListOf<BatchedEmissionFactorDigestModifier>(
            MinimumBatchedEmissionFactorDigestAdjuster(27.6),
            PercentBatchedEmissionFactorDigestAdjuster(4.3),
            TieredBatchedEmissionFactorDigestAdjuster(
                mutableListOf<Double>(5011.0, 5847.0, 6025.0, 7569.0),
                mutableListOf<Double>(1.11, 1.04, 0.83, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 8896.7,
    )
}
