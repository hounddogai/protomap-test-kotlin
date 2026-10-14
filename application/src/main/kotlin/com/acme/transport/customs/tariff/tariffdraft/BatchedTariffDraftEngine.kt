package com.acme.transport.customs.tariff.tariffdraft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched tariff draft calculation. */
interface BatchedTariffDraftModifier {
    fun describe(): String
    fun adjust(item: BatchedTariffDraft, value: Double): Double
}

data class TieredBatchedTariffDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedTariffDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedTariffDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class BatchedTariffDraftCreatedAtOffsetAdjuster(var weight: Double) : BatchedTariffDraftModifier {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: BatchedTariffDraft, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class CapBatchedTariffDraftAdjuster(var cap: Double) : BatchedTariffDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedTariffDraft, value: Double): Double = minOf(value, cap)
}

data class MinimumBatchedTariffDraftAdjuster(var minimum: Double) : BatchedTariffDraftModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedTariffDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBatchedTariffDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedTariffDraftModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedTariffDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBatchedTariffDraftAdjuster(var step: Double) : BatchedTariffDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedTariffDraft, value: Double): Double = roundTo(value, step)
}

/** Runs the batched tariff draft steps in order and keeps a trace of each. */
data class BatchedTariffDraftEngine(
    var steps: MutableList<BatchedTariffDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedTariffDraft): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedTariffDraftModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedTariffDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedTariffDraftEngine(flags: Registry): BatchedTariffDraftEngine {
    return BatchedTariffDraftEngine(
        steps = mutableListOf<BatchedTariffDraftModifier>(
            TieredBatchedTariffDraftAdjuster(
                mutableListOf<Double>(7716.0, 8412.0, 9818.0),
                mutableListOf<Double>(0.8, 1.14, 0.95),
            ),
            BatchedTariffDraftCreatedAtOffsetAdjuster(0.38),
            CapBatchedTariffDraftAdjuster(17792.0),
            MinimumBatchedTariffDraftAdjuster(38.9),
            FlaggedBatchedTariffDraftAdjuster(flags, "batched-tariff-draft-legacy", -6.1),
            RoundingBatchedTariffDraftAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 42351.4,
    )
}
