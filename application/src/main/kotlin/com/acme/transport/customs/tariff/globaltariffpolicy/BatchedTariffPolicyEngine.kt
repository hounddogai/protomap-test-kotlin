package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched tariff policy calculation. */
interface BatchedTariffPolicyFactor {
    fun describe(): String
    fun adjust(item: BatchedTariffPolicy, value: Double): Double
}

data class PercentBatchedTariffPolicyAdjuster(var percent: Double) : BatchedTariffPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedTariffPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredBatchedTariffPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedTariffPolicyFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedTariffPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingBatchedTariffPolicyAdjuster(var step: Double) : BatchedTariffPolicyFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedTariffPolicy, value: Double): Double = roundTo(value, step)
}

data class NameBatchedTariffPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedTariffPolicyFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: BatchedTariffPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBatchedTariffPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedTariffPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedTariffPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched tariff policy steps in order and keeps a trace of each. */
data class BatchedTariffPolicyEngine(
    var steps: MutableList<BatchedTariffPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedTariffPolicy): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedTariffPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedTariffPolicyEngine(flags: Registry): BatchedTariffPolicyEngine {
    return BatchedTariffPolicyEngine(
        steps = mutableListOf<BatchedTariffPolicyFactor>(
            PercentBatchedTariffPolicyAdjuster(19.9),
            TieredBatchedTariffPolicyAdjuster(
                mutableListOf<Double>(768.0, 3462.0),
                mutableListOf<Double>(1.06, 1.12),
            ),
            RoundingBatchedTariffPolicyAdjuster(1.0),
            NameBatchedTariffPolicyAdjuster(
                mutableMapOf<String, Double>("south" to 1.28, "east" to 0.83, "north" to 0.87),
            ),
            FlaggedBatchedTariffPolicyAdjuster(flags, "batched-tariff-policy-batched", 5.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 33827.6,
    )
}
