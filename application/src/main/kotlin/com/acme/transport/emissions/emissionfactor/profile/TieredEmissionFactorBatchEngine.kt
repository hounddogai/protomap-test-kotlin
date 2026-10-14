package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered emission factor batch calculation. */
interface TieredEmissionFactorBatchStep {
    fun describe(): String
    fun adjust(item: TieredEmissionFactorBatch, value: Double): Double
}

data class DepotTieredEmissionFactorBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredEmissionFactorBatchStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: TieredEmissionFactorBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentTieredEmissionFactorBatchAdjuster(var percent: Double) : TieredEmissionFactorBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredEmissionFactorBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingTieredEmissionFactorBatchAdjuster(var step: Double) : TieredEmissionFactorBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredEmissionFactorBatch, value: Double): Double = roundTo(value, step)
}

data class TieredTieredEmissionFactorBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredEmissionFactorBatchStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredEmissionFactorBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumTieredEmissionFactorBatchAdjuster(var minimum: Double) : TieredEmissionFactorBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredEmissionFactorBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered emission factor batch steps in order and keeps a trace of each. */
data class TieredEmissionFactorBatchEngine(
    var steps: MutableList<TieredEmissionFactorBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredEmissionFactorBatch): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredEmissionFactorBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredEmissionFactorBatchEngine(): TieredEmissionFactorBatchEngine {
    return TieredEmissionFactorBatchEngine(
        steps = mutableListOf<TieredEmissionFactorBatchStep>(
            DepotTieredEmissionFactorBatchAdjuster(
                mutableMapOf<String, Double>("online" to 0.95, "south" to 0.78),
            ),
            PercentTieredEmissionFactorBatchAdjuster(10.0),
            RoundingTieredEmissionFactorBatchAdjuster(1.0),
            TieredTieredEmissionFactorBatchAdjuster(
                mutableListOf<Double>(3033.0, 7403.0, 8828.0),
                mutableListOf<Double>(1.18, 1.11, 0.94),
            ),
            MinimumTieredEmissionFactorBatchAdjuster(15.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 35556.9,
    )
}
