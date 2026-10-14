package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched drop digest calculation. */
interface BatchedDropDigestAdjuster {
    fun describe(): String
    fun adjust(item: BatchedDropDigest, value: Double): Double
}

data class CountryBatchedDropDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedDropDigestAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: BatchedDropDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BatchedDropDigestMarginOffsetAdjuster(var weight: Double) : BatchedDropDigestAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: BatchedDropDigest, value: Double): Double = value + item.margin * weight
}

data class FlaggedBatchedDropDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedDropDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedDropDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched drop digest steps in order and keeps a trace of each. */
data class BatchedDropDigestEngine(
    var steps: MutableList<BatchedDropDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedDropDigest): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedDropDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedDropDigestEngine(flags: Registry): BatchedDropDigestEngine {
    return BatchedDropDigestEngine(
        steps = mutableListOf<BatchedDropDigestAdjuster>(
            CountryBatchedDropDigestAdjuster(
                mutableMapOf<String, Double>("west" to 1.07, "east" to 0.88, "retail" to 1.36),
            ),
            BatchedDropDigestMarginOffsetAdjuster(1.14),
            FlaggedBatchedDropDigestAdjuster(flags, "batched-drop-digest-capped", -3.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 1916.9,
    )
}
