package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.kit.flags.Registry

/** One step of the batched emission factor request calculation. */
interface BatchedEmissionFactorRequestFactor {
    fun describe(): String
    fun adjust(item: BatchedEmissionFactorRequest, value: Double): Double
}

data class MinimumBatchedEmissionFactorRequestAdjuster(
    var minimum: Double,
) : BatchedEmissionFactorRequestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedEmissionFactorRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBatchedEmissionFactorRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedEmissionFactorRequestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedEmissionFactorRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredBatchedEmissionFactorRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedEmissionFactorRequestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedEmissionFactorRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched emission factor request steps in order and keeps a trace of each. */
data class BatchedEmissionFactorRequestEngine(
    var steps: MutableList<BatchedEmissionFactorRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedEmissionFactorRequest): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedEmissionFactorRequestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedEmissionFactorRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedEmissionFactorRequestEngine(flags: Registry): BatchedEmissionFactorRequestEngine {
    return BatchedEmissionFactorRequestEngine(
        steps = mutableListOf<BatchedEmissionFactorRequestFactor>(
            MinimumBatchedEmissionFactorRequestAdjuster(30.9),
            FlaggedBatchedEmissionFactorRequestAdjuster(flags, "batched-emission-factor-request-regional", 2.3),
            TieredBatchedEmissionFactorRequestAdjuster(
                mutableListOf<Double>(3207.0, 3370.0, 6270.0, 8415.0),
                mutableListOf<Double>(1.07, 1.0, 1.18, 0.8),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 9732.3,
    )
}
