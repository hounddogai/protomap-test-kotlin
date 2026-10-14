package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default accessorial allocation calculation. */
interface DefaultAccessorialAllocationFactor {
    fun describe(): String
    fun adjust(item: DefaultAccessorialAllocation, value: Double): Double
}

data class PercentDefaultAccessorialAllocationAdjuster(
    var percent: Double,
) : DefaultAccessorialAllocationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultAccessorialAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDefaultAccessorialAllocationAdjuster(var cap: Double) : DefaultAccessorialAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultAccessorialAllocation, value: Double): Double = minOf(value, cap)
}

data class MinimumDefaultAccessorialAllocationAdjuster(
    var minimum: Double,
) : DefaultAccessorialAllocationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultAccessorialAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionDefaultAccessorialAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultAccessorialAllocationFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: DefaultAccessorialAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDefaultAccessorialAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultAccessorialAllocationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultAccessorialAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default accessorial allocation steps in order and keeps a trace of each. */
data class DefaultAccessorialAllocationEngine(
    var steps: MutableList<DefaultAccessorialAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultAccessorialAllocation): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultAccessorialAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultAccessorialAllocationEngine(): DefaultAccessorialAllocationEngine {
    return DefaultAccessorialAllocationEngine(
        steps = mutableListOf<DefaultAccessorialAllocationFactor>(
            PercentDefaultAccessorialAllocationAdjuster(2.9),
            CapDefaultAccessorialAllocationAdjuster(9070.0),
            MinimumDefaultAccessorialAllocationAdjuster(9.2),
            RegionDefaultAccessorialAllocationAdjuster(mutableMapOf<String, Double>("north" to 1.28)),
            TieredDefaultAccessorialAllocationAdjuster(
                mutableListOf<Double>(1755.0, 5580.0),
                mutableListOf<Double>(0.85, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 72161.5,
    )
}
