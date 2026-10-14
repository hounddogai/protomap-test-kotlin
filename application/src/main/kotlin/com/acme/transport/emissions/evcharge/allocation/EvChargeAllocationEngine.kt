package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the ev charge allocation calculation. */
interface EvChargeAllocationAdjuster {
    fun describe(): String
    fun adjust(item: EvChargeAllocation, value: Double): Double
}

data class MinimumEvChargeAllocationAdjuster(var minimum: Double) : EvChargeAllocationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapEvChargeAllocationAdjuster(var cap: Double) : EvChargeAllocationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeAllocation, value: Double): Double = minOf(value, cap)
}

data class FlaggedEvChargeAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeAllocationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredEvChargeAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeAllocationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the ev charge allocation steps in order and keeps a trace of each. */
data class EvChargeAllocationEngine(
    var steps: MutableList<EvChargeAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: EvChargeAllocation): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EvChargeAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultEvChargeAllocationEngine(flags: Registry): EvChargeAllocationEngine {
    return EvChargeAllocationEngine(
        steps = mutableListOf<EvChargeAllocationAdjuster>(
            MinimumEvChargeAllocationAdjuster(46.6),
            CapEvChargeAllocationAdjuster(49851.0),
            FlaggedEvChargeAllocationAdjuster(flags, "ev-charge-allocation-batched", 1.0),
            TieredEvChargeAllocationAdjuster(
                mutableListOf<Double>(2524.0, 2648.0, 5983.0, 6801.0),
                mutableListOf<Double>(0.86, 1.01, 0.8, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 29298.2,
    )
}
