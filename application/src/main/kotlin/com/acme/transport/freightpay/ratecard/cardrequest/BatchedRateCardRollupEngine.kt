package com.acme.transport.freightpay.ratecard.cardrequest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched rate card rollup calculation. */
interface BatchedRateCardRollupStep {
    fun describe(): String
    fun adjust(item: BatchedRateCardRollup, value: Double): Double
}

data class MinimumBatchedRateCardRollupAdjuster(var minimum: Double) : BatchedRateCardRollupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedRateCardRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BatchedRateCardRollupEtaMinutesOffsetAdjuster(var weight: Double) : BatchedRateCardRollupStep {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: BatchedRateCardRollup, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class NameBatchedRateCardRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedRateCardRollupStep {
    override fun describe(): String = "by name"

    override fun adjust(item: BatchedRateCardRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBatchedRateCardRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedRateCardRollupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedRateCardRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBatchedRateCardRollupAdjuster(var step: Double) : BatchedRateCardRollupStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedRateCardRollup, value: Double): Double = roundTo(value, step)
}

data class TieredBatchedRateCardRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedRateCardRollupStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedRateCardRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched rate card rollup steps in order and keeps a trace of each. */
data class BatchedRateCardRollupEngine(
    var steps: MutableList<BatchedRateCardRollupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedRateCardRollup): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedRateCardRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedRateCardRollupEngine(flags: Registry): BatchedRateCardRollupEngine {
    return BatchedRateCardRollupEngine(
        steps = mutableListOf<BatchedRateCardRollupStep>(
            MinimumBatchedRateCardRollupAdjuster(18.2),
            BatchedRateCardRollupEtaMinutesOffsetAdjuster(1.07),
            NameBatchedRateCardRollupAdjuster(mutableMapOf<String, Double>("online" to 0.99)),
            FlaggedBatchedRateCardRollupAdjuster(flags, "batched-rate-card-rollup-legacy", 4.8),
            RoundingBatchedRateCardRollupAdjuster(0.1),
            TieredBatchedRateCardRollupAdjuster(
                mutableListOf<Double>(1816.0, 4042.0, 5500.0, 6002.0),
                mutableListOf<Double>(1.07, 1.07, 0.86, 0.88),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 92621.6,
    )
}
