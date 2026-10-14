package com.acme.transport.crewing.swap.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient swap batch calculation. */
interface LenientSwapBatchStep {
    fun describe(): String
    fun adjust(item: LenientSwapBatch, value: Double): Double
}

data class TieredLenientSwapBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientSwapBatchStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientSwapBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLenientSwapBatchAdjuster(var percent: Double) : LenientSwapBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientSwapBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLenientSwapBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientSwapBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientSwapBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LenientSwapBatchScoreOffsetAdjuster(var weight: Double) : LenientSwapBatchStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: LenientSwapBatch, value: Double): Double = value + item.score * weight
}

data class CapLenientSwapBatchAdjuster(var cap: Double) : LenientSwapBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientSwapBatch, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient swap batch steps in order and keeps a trace of each. */
data class LenientSwapBatchEngine(
    var steps: MutableList<LenientSwapBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientSwapBatch): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientSwapBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientSwapBatchEngine(flags: Registry): LenientSwapBatchEngine {
    return LenientSwapBatchEngine(
        steps = mutableListOf<LenientSwapBatchStep>(
            TieredLenientSwapBatchAdjuster(
                mutableListOf<Double>(248.0, 4360.0, 5736.0),
                mutableListOf<Double>(0.89, 1.04, 0.9),
            ),
            PercentLenientSwapBatchAdjuster(-21.5),
            FlaggedLenientSwapBatchAdjuster(flags, "lenient-swap-batch-weighted", -6.2),
            LenientSwapBatchScoreOffsetAdjuster(0.52),
            CapLenientSwapBatchAdjuster(12012.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 7668.2,
    )
}
