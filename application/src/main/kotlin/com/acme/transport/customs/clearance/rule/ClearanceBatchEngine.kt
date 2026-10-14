package com.acme.transport.customs.clearance.rule

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the clearance batch calculation. */
interface ClearanceBatchFactor {
    fun describe(): String
    fun adjust(item: ClearanceBatch, value: Double): Double
}

data class MinimumClearanceBatchAdjuster(var minimum: Double) : ClearanceBatchFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearanceBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredClearanceBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceBatchFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapClearanceBatchAdjuster(var cap: Double) : ClearanceBatchFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceBatch, value: Double): Double = minOf(value, cap)
}

/** Runs the clearance batch steps in order and keeps a trace of each. */
data class ClearanceBatchEngine(
    var steps: MutableList<ClearanceBatchFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ClearanceBatch): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ClearanceBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultClearanceBatchEngine(): ClearanceBatchEngine {
    return ClearanceBatchEngine(
        steps = mutableListOf<ClearanceBatchFactor>(
            MinimumClearanceBatchAdjuster(48.7),
            TieredClearanceBatchAdjuster(
                mutableListOf<Double>(568.0, 3619.0, 5442.0, 7926.0),
                mutableListOf<Double>(0.93, 0.86, 0.94, 0.94),
            ),
            CapClearanceBatchAdjuster(41938.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 70172.0,
    )
}
