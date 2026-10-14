package com.acme.transport.linehaul.tender.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback tender batch calculation. */
interface FallbackTenderBatchModifier {
    fun describe(): String
    fun adjust(item: FallbackTenderBatch, value: Double): Double
}

data class NameFallbackTenderBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackTenderBatchModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: FallbackTenderBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredFallbackTenderBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackTenderBatchModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackTenderBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingFallbackTenderBatchAdjuster(var step: Double) : FallbackTenderBatchModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackTenderBatch, value: Double): Double = roundTo(value, step)
}

data class PercentFallbackTenderBatchAdjuster(var percent: Double) : FallbackTenderBatchModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackTenderBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedFallbackTenderBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackTenderBatchModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackTenderBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFallbackTenderBatchAdjuster(var minimum: Double) : FallbackTenderBatchModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackTenderBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback tender batch steps in order and keeps a trace of each. */
data class FallbackTenderBatchEngine(
    var steps: MutableList<FallbackTenderBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackTenderBatch): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackTenderBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackTenderBatchEngine(flags: Registry): FallbackTenderBatchEngine {
    return FallbackTenderBatchEngine(
        steps = mutableListOf<FallbackTenderBatchModifier>(
            NameFallbackTenderBatchAdjuster(
                mutableMapOf<String, Double>("south" to 1.15, "retail" to 0.81, "north" to 1.23),
            ),
            TieredFallbackTenderBatchAdjuster(
                mutableListOf<Double>(4280.0, 5420.0, 7725.0),
                mutableListOf<Double>(0.91, 0.95, 1.18),
            ),
            RoundingFallbackTenderBatchAdjuster(5.0),
            PercentFallbackTenderBatchAdjuster(-3.1),
            FlaggedFallbackTenderBatchAdjuster(flags, "fallback-tender-batch-pending", -7.0),
            MinimumFallbackTenderBatchAdjuster(20.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 32765.7,
    )
}
