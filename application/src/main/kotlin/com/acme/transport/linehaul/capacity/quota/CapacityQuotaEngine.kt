package com.acme.transport.linehaul.capacity.quota

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capacity quota calculation. */
interface CapacityQuotaAdjuster {
    fun describe(): String
    fun adjust(item: CapacityQuota, value: Double): Double
}

data class CapCapacityQuotaAdjuster(var cap: Double) : CapacityQuotaAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityQuota, value: Double): Double = minOf(value, cap)
}

data class TieredCapacityQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityQuotaAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCapacityQuotaAdjuster(var percent: Double) : CapacityQuotaAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CapacityQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capacity quota steps in order and keeps a trace of each. */
data class CapacityQuotaEngine(
    var steps: MutableList<CapacityQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CapacityQuota): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CapacityQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCapacityQuotaEngine(): CapacityQuotaEngine {
    return CapacityQuotaEngine(
        steps = mutableListOf<CapacityQuotaAdjuster>(
            CapCapacityQuotaAdjuster(7660.0),
            TieredCapacityQuotaAdjuster(mutableListOf<Double>(88.0, 4318.0), mutableListOf<Double>(1.03, 0.97)),
            PercentCapacityQuotaAdjuster(9.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 85738.6,
    )
}
