package com.acme.transport.linehaul.tender.exception

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict tender allocation calculation. */
interface StrictTenderAllocationStep {
    fun describe(): String
    fun adjust(item: StrictTenderAllocation, value: Double): Double
}

data class TieredStrictTenderAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictTenderAllocationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictTenderAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameStrictTenderAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictTenderAllocationStep {
    override fun describe(): String = "by name"

    override fun adjust(item: StrictTenderAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapStrictTenderAllocationAdjuster(var cap: Double) : StrictTenderAllocationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictTenderAllocation, value: Double): Double = minOf(value, cap)
}

/** Runs the strict tender allocation steps in order and keeps a trace of each. */
data class StrictTenderAllocationEngine(
    var steps: MutableList<StrictTenderAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictTenderAllocation): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictTenderAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictTenderAllocationEngine(): StrictTenderAllocationEngine {
    return StrictTenderAllocationEngine(
        steps = mutableListOf<StrictTenderAllocationStep>(
            TieredStrictTenderAllocationAdjuster(
                mutableListOf<Double>(588.0, 943.0, 5609.0),
                mutableListOf<Double>(1.12, 1.15, 0.91),
            ),
            NameStrictTenderAllocationAdjuster(mutableMapOf<String, Double>("online" to 1.24, "retail" to 1.38)),
            CapStrictTenderAllocationAdjuster(44950.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 96686.1,
    )
}
