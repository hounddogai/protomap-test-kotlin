package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched rate card threshold calculation. */
interface BatchedRateCardThresholdAdjuster {
    fun describe(): String
    fun adjust(item: BatchedRateCardThreshold, value: Double): Double
}

data class RoundingBatchedRateCardThresholdAdjuster(var step: Double) : BatchedRateCardThresholdAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedRateCardThreshold, value: Double): Double = roundTo(value, step)
}

data class CapBatchedRateCardThresholdAdjuster(var cap: Double) : BatchedRateCardThresholdAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedRateCardThreshold, value: Double): Double = minOf(value, cap)
}

data class TieredBatchedRateCardThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedRateCardThresholdAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedRateCardThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedBatchedRateCardThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedRateCardThresholdAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedRateCardThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched rate card threshold steps in order and keeps a trace of each. */
data class BatchedRateCardThresholdEngine(
    var steps: MutableList<BatchedRateCardThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedRateCardThreshold): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedRateCardThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedRateCardThresholdEngine(flags: Registry): BatchedRateCardThresholdEngine {
    return BatchedRateCardThresholdEngine(
        steps = mutableListOf<BatchedRateCardThresholdAdjuster>(
            RoundingBatchedRateCardThresholdAdjuster(5.0),
            CapBatchedRateCardThresholdAdjuster(45019.0),
            TieredBatchedRateCardThresholdAdjuster(
                mutableListOf<Double>(3276.0, 8118.0, 8596.0),
                mutableListOf<Double>(1.2, 1.19, 0.99),
            ),
            FlaggedBatchedRateCardThresholdAdjuster(flags, "batched-rate-card-threshold-primary", 0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 41739.9,
    )
}
