package com.acme.transport.customs.hold.batch

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the hold batch calculation. */
interface HoldBatchModifier {
    fun describe(): String
    fun adjust(item: HoldBatch, value: Double): Double
}

data class TieredHoldBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HoldBatchModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HoldBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentHoldBatchAdjuster(var percent: Double) : HoldBatchModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HoldBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumHoldBatchAdjuster(var minimum: Double) : HoldBatchModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HoldBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the hold batch steps in order and keeps a trace of each. */
data class HoldBatchEngine(
    var steps: MutableList<HoldBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HoldBatch): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HoldBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHoldBatchEngine(): HoldBatchEngine {
    return HoldBatchEngine(
        steps = mutableListOf<HoldBatchModifier>(
            TieredHoldBatchAdjuster(mutableListOf<Double>(3481.0, 6102.0), mutableListOf<Double>(1.01, 0.87)),
            PercentHoldBatchAdjuster(3.4),
            MinimumHoldBatchAdjuster(23.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 6789.6,
    )
}
