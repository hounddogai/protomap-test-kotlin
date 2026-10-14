package com.acme.transport.customs.tariff.tariffbatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped tariff batch calculation. */
interface CappedTariffBatchModifier {
    fun describe(): String
    fun adjust(item: CappedTariffBatch, value: Double): Double
}

data class FlaggedCappedTariffBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedTariffBatchModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedTariffBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCappedTariffBatchAdjuster(var minimum: Double) : CappedTariffBatchModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedTariffBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCappedTariffBatchAdjuster(var cap: Double) : CappedTariffBatchModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedTariffBatch, value: Double): Double = minOf(value, cap)
}

data class CappedTariffBatchBudgetOffsetAdjuster(var weight: Double) : CappedTariffBatchModifier {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: CappedTariffBatch, value: Double): Double = value + item.budget * weight
}

data class TitleCappedTariffBatchAdjuster(var factors: MutableMap<String, Double>) : CappedTariffBatchModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: CappedTariffBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped tariff batch steps in order and keeps a trace of each. */
data class CappedTariffBatchEngine(
    var steps: MutableList<CappedTariffBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedTariffBatch): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedTariffBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedTariffBatchEngine(flags: Registry): CappedTariffBatchEngine {
    return CappedTariffBatchEngine(
        steps = mutableListOf<CappedTariffBatchModifier>(
            FlaggedCappedTariffBatchAdjuster(flags, "capped-tariff-batch-batched", 1.2),
            MinimumCappedTariffBatchAdjuster(15.8),
            CapCappedTariffBatchAdjuster(16230.0),
            CappedTariffBatchBudgetOffsetAdjuster(0.4),
            TitleCappedTariffBatchAdjuster(mutableMapOf<String, Double>("online" to 1.2, "east" to 1.31)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 20578.9,
    )
}
