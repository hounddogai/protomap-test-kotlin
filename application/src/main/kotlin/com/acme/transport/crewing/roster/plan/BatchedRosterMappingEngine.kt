package com.acme.transport.crewing.roster.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched roster mapping calculation. */
interface BatchedRosterMappingFactor {
    fun describe(): String
    fun adjust(item: BatchedRosterMapping, value: Double): Double
}

data class FlaggedBatchedRosterMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedRosterMappingFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedRosterMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredBatchedRosterMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedRosterMappingFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedRosterMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class BatchedRosterMappingGraceDaysOffsetAdjuster(var weight: Double) : BatchedRosterMappingFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: BatchedRosterMapping, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

/** Runs the batched roster mapping steps in order and keeps a trace of each. */
data class BatchedRosterMappingEngine(
    var steps: MutableList<BatchedRosterMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedRosterMapping): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedRosterMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedRosterMappingEngine(flags: Registry): BatchedRosterMappingEngine {
    return BatchedRosterMappingEngine(
        steps = mutableListOf<BatchedRosterMappingFactor>(
            FlaggedBatchedRosterMappingAdjuster(flags, "batched-roster-mapping-lenient", 6.0),
            TieredBatchedRosterMappingAdjuster(
                mutableListOf<Double>(607.0, 1221.0),
                mutableListOf<Double>(0.95, 1.03),
            ),
            BatchedRosterMappingGraceDaysOffsetAdjuster(1.75),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 6969.4,
    )
}
