package com.acme.transport.terminal.dwell.dwellgroup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched dwell group calculation. */
interface BatchedDwellGroupAdjuster {
    fun describe(): String
    fun adjust(item: BatchedDwellGroup, value: Double): Double
}

data class CapBatchedDwellGroupAdjuster(var cap: Double) : BatchedDwellGroupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedDwellGroup, value: Double): Double = minOf(value, cap)
}

data class FlaggedBatchedDwellGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedDwellGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedDwellGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SourceBatchedDwellGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedDwellGroupAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: BatchedDwellGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBatchedDwellGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedDwellGroupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedDwellGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumBatchedDwellGroupAdjuster(var minimum: Double) : BatchedDwellGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedDwellGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched dwell group steps in order and keeps a trace of each. */
data class BatchedDwellGroupEngine(
    var steps: MutableList<BatchedDwellGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedDwellGroup): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedDwellGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedDwellGroupEngine(flags: Registry): BatchedDwellGroupEngine {
    return BatchedDwellGroupEngine(
        steps = mutableListOf<BatchedDwellGroupAdjuster>(
            CapBatchedDwellGroupAdjuster(30560.0),
            FlaggedBatchedDwellGroupAdjuster(flags, "batched-dwell-group-legacy", -8.0),
            SourceBatchedDwellGroupAdjuster(mutableMapOf<String, Double>("east" to 1.03, "central" to 1.11)),
            TieredBatchedDwellGroupAdjuster(
                mutableListOf<Double>(1450.0, 7220.0),
                mutableListOf<Double>(1.12, 0.84),
            ),
            MinimumBatchedDwellGroupAdjuster(49.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 43267.4,
    )
}
