package com.acme.transport.lastmile.drop.profile

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched drop profile calculation. */
interface BatchedDropProfileModifier {
    fun describe(): String
    fun adjust(item: BatchedDropProfile, value: Double): Double
}

data class TieredBatchedDropProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedDropProfileModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedDropProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentBatchedDropProfileAdjuster(var percent: Double) : BatchedDropProfileModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedDropProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapBatchedDropProfileAdjuster(var cap: Double) : BatchedDropProfileModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedDropProfile, value: Double): Double = minOf(value, cap)
}

data class MinimumBatchedDropProfileAdjuster(var minimum: Double) : BatchedDropProfileModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedDropProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingBatchedDropProfileAdjuster(var step: Double) : BatchedDropProfileModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedDropProfile, value: Double): Double = roundTo(value, step)
}

data class LabelBatchedDropProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedDropProfileModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedDropProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched drop profile steps in order and keeps a trace of each. */
data class BatchedDropProfileEngine(
    var steps: MutableList<BatchedDropProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedDropProfile): Double {
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

    fun evaluateAll(items: MutableList<BatchedDropProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedDropProfileEngine(): BatchedDropProfileEngine {
    return BatchedDropProfileEngine(
        steps = mutableListOf<BatchedDropProfileModifier>(
            TieredBatchedDropProfileAdjuster(
                mutableListOf<Double>(416.0, 2671.0, 3241.0, 4232.0),
                mutableListOf<Double>(0.93, 1.08, 1.16, 0.94),
            ),
            PercentBatchedDropProfileAdjuster(15.5),
            CapBatchedDropProfileAdjuster(28306.0),
            MinimumBatchedDropProfileAdjuster(31.6),
            RoundingBatchedDropProfileAdjuster(0.05),
            LabelBatchedDropProfileAdjuster(
                mutableMapOf<String, Double>("west" to 0.89, "central" to 0.77, "south" to 1.05),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 8126.6,
    )
}
