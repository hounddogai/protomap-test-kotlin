package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional drop allocation calculation. */
interface RegionalDropAllocationModifier {
    fun describe(): String
    fun adjust(item: RegionalDropAllocation, value: Double): Double
}

data class TieredRegionalDropAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalDropAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalDropAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionalDropAllocationAmountOffsetAdjuster(var weight: Double) : RegionalDropAllocationModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: RegionalDropAllocation, value: Double): Double = value + item.amount * weight
}

data class CapRegionalDropAllocationAdjuster(var cap: Double) : RegionalDropAllocationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDropAllocation, value: Double): Double = minOf(value, cap)
}

data class RoundingRegionalDropAllocationAdjuster(var step: Double) : RegionalDropAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDropAllocation, value: Double): Double = roundTo(value, step)
}

/** Runs the regional drop allocation steps in order and keeps a trace of each. */
data class RegionalDropAllocationEngine(
    var steps: MutableList<RegionalDropAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalDropAllocation): Double {
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

    fun evaluateAll(items: MutableList<RegionalDropAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalDropAllocationEngine(): RegionalDropAllocationEngine {
    return RegionalDropAllocationEngine(
        steps = mutableListOf<RegionalDropAllocationModifier>(
            TieredRegionalDropAllocationAdjuster(
                mutableListOf<Double>(5792.0, 9496.0),
                mutableListOf<Double>(1.12, 0.89),
            ),
            RegionalDropAllocationAmountOffsetAdjuster(0.84),
            CapRegionalDropAllocationAdjuster(31451.0),
            RoundingRegionalDropAllocationAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 85486.8,
    )
}
