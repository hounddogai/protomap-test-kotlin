package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global scorecard allocation calculation. */
interface GlobalScorecardAllocationStep {
    fun describe(): String
    fun adjust(item: GlobalScorecardAllocation, value: Double): Double
}

data class TieredGlobalScorecardAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalScorecardAllocationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalScorecardAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumGlobalScorecardAllocationAdjuster(var minimum: Double) : GlobalScorecardAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalScorecardAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapGlobalScorecardAllocationAdjuster(var cap: Double) : GlobalScorecardAllocationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalScorecardAllocation, value: Double): Double = minOf(value, cap)
}

/** Runs the global scorecard allocation steps in order and keeps a trace of each. */
data class GlobalScorecardAllocationEngine(
    var steps: MutableList<GlobalScorecardAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalScorecardAllocation): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalScorecardAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalScorecardAllocationEngine(): GlobalScorecardAllocationEngine {
    return GlobalScorecardAllocationEngine(
        steps = mutableListOf<GlobalScorecardAllocationStep>(
            TieredGlobalScorecardAllocationAdjuster(
                mutableListOf<Double>(361.0, 372.0, 2157.0),
                mutableListOf<Double>(1.1, 1.01, 0.99),
            ),
            MinimumGlobalScorecardAllocationAdjuster(30.6),
            CapGlobalScorecardAllocationAdjuster(21797.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 67712.5,
    )
}
