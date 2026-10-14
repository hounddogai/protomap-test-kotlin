package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default tariff batch calculation. */
interface DefaultTariffBatchStep {
    fun describe(): String
    fun adjust(item: DefaultTariffBatch, value: Double): Double
}

data class CapDefaultTariffBatchAdjuster(var cap: Double) : DefaultTariffBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultTariffBatch, value: Double): Double = minOf(value, cap)
}

data class FlaggedDefaultTariffBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultTariffBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultTariffBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DefaultTariffBatchMaximumOffsetAdjuster(var weight: Double) : DefaultTariffBatchStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: DefaultTariffBatch, value: Double): Double = value + item.maximum * weight
}

data class RegionDefaultTariffBatchAdjuster(var factors: MutableMap<String, Double>) : DefaultTariffBatchStep {
    override fun describe(): String = "by region"

    override fun adjust(item: DefaultTariffBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDefaultTariffBatchAdjuster(var step: Double) : DefaultTariffBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultTariffBatch, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultTariffBatchAdjuster(var minimum: Double) : DefaultTariffBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultTariffBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default tariff batch steps in order and keeps a trace of each. */
data class DefaultTariffBatchEngine(
    var steps: MutableList<DefaultTariffBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultTariffBatch): Double {
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

    fun evaluateAll(items: MutableList<DefaultTariffBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultTariffBatchEngine(flags: Registry): DefaultTariffBatchEngine {
    return DefaultTariffBatchEngine(
        steps = mutableListOf<DefaultTariffBatchStep>(
            CapDefaultTariffBatchAdjuster(47629.0),
            FlaggedDefaultTariffBatchAdjuster(flags, "default-tariff-batch-fallback", 3.2),
            DefaultTariffBatchMaximumOffsetAdjuster(0.41),
            RegionDefaultTariffBatchAdjuster(
                mutableMapOf<String, Double>("south" to 1.33, "east" to 1.17, "online" to 0.94),
            ),
            RoundingDefaultTariffBatchAdjuster(1.0),
            MinimumDefaultTariffBatchAdjuster(43.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 5448.8,
    )
}
