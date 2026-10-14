package com.acme.transport.customs.tariff.tariffbatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched tariff variance calculation. */
interface BatchedTariffVarianceFactor {
    fun describe(): String
    fun adjust(item: BatchedTariffVariance, value: Double): Double
}

data class BatchedTariffVarianceFuelLitersOffsetAdjuster(var weight: Double) : BatchedTariffVarianceFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: BatchedTariffVariance, value: Double): Double = value + item.fuelLiters * weight
}

data class CapBatchedTariffVarianceAdjuster(var cap: Double) : BatchedTariffVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedTariffVariance, value: Double): Double = minOf(value, cap)
}

data class RoundingBatchedTariffVarianceAdjuster(var step: Double) : BatchedTariffVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedTariffVariance, value: Double): Double = roundTo(value, step)
}

data class FlaggedBatchedTariffVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedTariffVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedTariffVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredBatchedTariffVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedTariffVarianceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedTariffVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourceBatchedTariffVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedTariffVarianceFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: BatchedTariffVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched tariff variance steps in order and keeps a trace of each. */
data class BatchedTariffVarianceEngine(
    var steps: MutableList<BatchedTariffVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedTariffVariance): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedTariffVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedTariffVarianceEngine(flags: Registry): BatchedTariffVarianceEngine {
    return BatchedTariffVarianceEngine(
        steps = mutableListOf<BatchedTariffVarianceFactor>(
            BatchedTariffVarianceFuelLitersOffsetAdjuster(1.6),
            CapBatchedTariffVarianceAdjuster(49029.0),
            RoundingBatchedTariffVarianceAdjuster(1.0),
            FlaggedBatchedTariffVarianceAdjuster(flags, "batched-tariff-variance-default", 3.8),
            TieredBatchedTariffVarianceAdjuster(
                mutableListOf<Double>(677.0, 4671.0, 6207.0, 9539.0),
                mutableListOf<Double>(0.86, 1.07, 0.93, 1.06),
            ),
            SourceBatchedTariffVarianceAdjuster(mutableMapOf<String, Double>("central" to 0.97)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 28700.3,
    )
}
