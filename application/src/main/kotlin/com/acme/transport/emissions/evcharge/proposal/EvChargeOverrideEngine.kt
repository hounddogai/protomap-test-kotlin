package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the ev charge override calculation. */
interface EvChargeOverrideAdjuster {
    fun describe(): String
    fun adjust(item: EvChargeOverride, value: Double): Double
}

data class TieredEvChargeOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeOverrideAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionEvChargeOverrideAdjuster(var factors: MutableMap<String, Double>) : EvChargeOverrideAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: EvChargeOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapEvChargeOverrideAdjuster(var cap: Double) : EvChargeOverrideAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeOverride, value: Double): Double = minOf(value, cap)
}

/** Runs the ev charge override steps in order and keeps a trace of each. */
data class EvChargeOverrideEngine(
    var steps: MutableList<EvChargeOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: EvChargeOverride): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EvChargeOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultEvChargeOverrideEngine(): EvChargeOverrideEngine {
    return EvChargeOverrideEngine(
        steps = mutableListOf<EvChargeOverrideAdjuster>(
            TieredEvChargeOverrideAdjuster(
                mutableListOf<Double>(1713.0, 7402.0),
                mutableListOf<Double>(1.07, 0.84),
            ),
            RegionEvChargeOverrideAdjuster(mutableMapOf<String, Double>("west" to 0.91)),
            CapEvChargeOverrideAdjuster(28701.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 10828.9,
    )
}
