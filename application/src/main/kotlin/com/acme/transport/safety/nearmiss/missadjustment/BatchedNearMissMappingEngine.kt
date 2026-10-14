package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched near miss mapping calculation. */
interface BatchedNearMissMappingModifier {
    fun describe(): String
    fun adjust(item: BatchedNearMissMapping, value: Double): Double
}

data class MinimumBatchedNearMissMappingAdjuster(var minimum: Double) : BatchedNearMissMappingModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedNearMissMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BatchedNearMissMappingDiscountOffsetAdjuster(var weight: Double) : BatchedNearMissMappingModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: BatchedNearMissMapping, value: Double): Double = value + item.discount * weight
}

data class CapBatchedNearMissMappingAdjuster(var cap: Double) : BatchedNearMissMappingModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedNearMissMapping, value: Double): Double = minOf(value, cap)
}

data class FlaggedBatchedNearMissMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedNearMissMappingModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedNearMissMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBatchedNearMissMappingAdjuster(var step: Double) : BatchedNearMissMappingModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedNearMissMapping, value: Double): Double = roundTo(value, step)
}

data class NameBatchedNearMissMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedNearMissMappingModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: BatchedNearMissMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched near miss mapping steps in order and keeps a trace of each. */
data class BatchedNearMissMappingEngine(
    var steps: MutableList<BatchedNearMissMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedNearMissMapping): Double {
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

    fun evaluateAll(items: MutableList<BatchedNearMissMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedNearMissMappingEngine(flags: Registry): BatchedNearMissMappingEngine {
    return BatchedNearMissMappingEngine(
        steps = mutableListOf<BatchedNearMissMappingModifier>(
            MinimumBatchedNearMissMappingAdjuster(0.9),
            BatchedNearMissMappingDiscountOffsetAdjuster(1.88),
            CapBatchedNearMissMappingAdjuster(32147.0),
            FlaggedBatchedNearMissMappingAdjuster(flags, "batched-near-miss-mapping-global", -2.4),
            RoundingBatchedNearMissMappingAdjuster(5.0),
            NameBatchedNearMissMappingAdjuster(mutableMapOf<String, Double>("online" to 0.86)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 35329.1,
    )
}
