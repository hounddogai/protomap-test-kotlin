package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capacity allocation calculation. */
interface CapacityAllocationModifier {
    fun describe(): String
    fun adjust(item: CapacityAllocation, value: Double): Double
}

data class MinimumCapacityAllocationAdjuster(var minimum: Double) : CapacityAllocationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapacityAllocationThresholdOffsetAdjuster(var weight: Double) : CapacityAllocationModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: CapacityAllocation, value: Double): Double = value + item.threshold * weight
}

data class TieredCapacityAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCapacityAllocationAdjuster(var cap: Double) : CapacityAllocationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityAllocation, value: Double): Double = minOf(value, cap)
}

/** Runs the capacity allocation steps in order and keeps a trace of each. */
data class CapacityAllocationEngine(
    var steps: MutableList<CapacityAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CapacityAllocation): Double {
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

    fun evaluateAll(items: MutableList<CapacityAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCapacityAllocationEngine(): CapacityAllocationEngine {
    return CapacityAllocationEngine(
        steps = mutableListOf<CapacityAllocationModifier>(
            MinimumCapacityAllocationAdjuster(48.7),
            CapacityAllocationThresholdOffsetAdjuster(1.9),
            TieredCapacityAllocationAdjuster(
                mutableListOf<Double>(3242.0, 7648.0, 9452.0),
                mutableListOf<Double>(0.92, 0.93, 1.04),
            ),
            CapCapacityAllocationAdjuster(23136.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 74341.1,
    )
}
