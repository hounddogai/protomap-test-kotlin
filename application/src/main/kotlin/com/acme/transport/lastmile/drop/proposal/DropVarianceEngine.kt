package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the drop variance calculation. */
interface DropVarianceFactor {
    fun describe(): String
    fun adjust(item: DropVariance, value: Double): Double
}

data class CapDropVarianceAdjuster(var cap: Double) : DropVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DropVariance, value: Double): Double = minOf(value, cap)
}

data class DropVarianceBatchSizeOffsetAdjuster(var weight: Double) : DropVarianceFactor {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: DropVariance, value: Double): Double = value + item.batchSize.toDouble() * weight
}

data class TieredDropVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DropVarianceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DropVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the drop variance steps in order and keeps a trace of each. */
data class DropVarianceEngine(
    var steps: MutableList<DropVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DropVariance): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DropVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDropVarianceEngine(): DropVarianceEngine {
    return DropVarianceEngine(
        steps = mutableListOf<DropVarianceFactor>(
            CapDropVarianceAdjuster(36985.0),
            DropVarianceBatchSizeOffsetAdjuster(1.27),
            TieredDropVarianceAdjuster(
                mutableListOf<Double>(400.0, 2294.0, 5535.0, 9449.0),
                mutableListOf<Double>(0.99, 0.83, 0.97, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 82582.4,
    )
}
