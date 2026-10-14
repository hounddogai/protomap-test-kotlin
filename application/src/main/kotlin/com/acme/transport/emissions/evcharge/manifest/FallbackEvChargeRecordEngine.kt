package com.acme.transport.emissions.evcharge.manifest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback ev charge record calculation. */
interface FallbackEvChargeRecordStep {
    fun describe(): String
    fun adjust(item: FallbackEvChargeRecord, value: Double): Double
}

data class FallbackEvChargeRecordPriorityOffsetAdjuster(var weight: Double) : FallbackEvChargeRecordStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: FallbackEvChargeRecord, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class ChannelFallbackEvChargeRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackEvChargeRecordStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: FallbackEvChargeRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingFallbackEvChargeRecordAdjuster(var step: Double) : FallbackEvChargeRecordStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackEvChargeRecord, value: Double): Double = roundTo(value, step)
}

data class TieredFallbackEvChargeRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackEvChargeRecordStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackEvChargeRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback ev charge record steps in order and keeps a trace of each. */
data class FallbackEvChargeRecordEngine(
    var steps: MutableList<FallbackEvChargeRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackEvChargeRecord): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackEvChargeRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackEvChargeRecordEngine(): FallbackEvChargeRecordEngine {
    return FallbackEvChargeRecordEngine(
        steps = mutableListOf<FallbackEvChargeRecordStep>(
            FallbackEvChargeRecordPriorityOffsetAdjuster(1.07),
            ChannelFallbackEvChargeRecordAdjuster(mutableMapOf<String, Double>("retail" to 0.94)),
            RoundingFallbackEvChargeRecordAdjuster(0.25),
            TieredFallbackEvChargeRecordAdjuster(
                mutableListOf<Double>(2347.0, 8851.0, 8975.0),
                mutableListOf<Double>(1.16, 0.94, 0.99),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 53702.1,
    )
}
