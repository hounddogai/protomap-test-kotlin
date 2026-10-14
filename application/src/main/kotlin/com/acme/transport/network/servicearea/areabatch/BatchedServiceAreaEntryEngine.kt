package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched service area entry calculation. */
interface BatchedServiceAreaEntryModifier {
    fun describe(): String
    fun adjust(item: BatchedServiceAreaEntry, value: Double): Double
}

data class FlaggedBatchedServiceAreaEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedServiceAreaEntryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedServiceAreaEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBatchedServiceAreaEntryAdjuster(var step: Double) : BatchedServiceAreaEntryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedServiceAreaEntry, value: Double): Double = roundTo(value, step)
}

data class TieredBatchedServiceAreaEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedServiceAreaEntryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedServiceAreaEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched service area entry steps in order and keeps a trace of each. */
data class BatchedServiceAreaEntryEngine(
    var steps: MutableList<BatchedServiceAreaEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedServiceAreaEntry): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedServiceAreaEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedServiceAreaEntryEngine(flags: Registry): BatchedServiceAreaEntryEngine {
    return BatchedServiceAreaEntryEngine(
        steps = mutableListOf<BatchedServiceAreaEntryModifier>(
            FlaggedBatchedServiceAreaEntryAdjuster(flags, "batched-service-area-entry-primary", 6.5),
            RoundingBatchedServiceAreaEntryAdjuster(0.05),
            TieredBatchedServiceAreaEntryAdjuster(
                mutableListOf<Double>(6755.0, 9960.0),
                mutableListOf<Double>(1.13, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 45052.7,
    )
}
