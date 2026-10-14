package com.acme.transport.customs.inspection.allocation

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the inspection allocation calculation. */
interface InspectionAllocationFactor {
    fun describe(): String
    fun adjust(item: InspectionAllocation, value: Double): Double
}

data class MinimumInspectionAllocationAdjuster(var minimum: Double) : InspectionAllocationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionInspectionAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : InspectionAllocationFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: InspectionAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class InspectionAllocationRetriesOffsetAdjuster(var weight: Double) : InspectionAllocationFactor {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: InspectionAllocation, value: Double): Double = value + item.retries.toDouble() * weight
}

/** Runs the inspection allocation steps in order and keeps a trace of each. */
data class InspectionAllocationEngine(
    var steps: MutableList<InspectionAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionAllocation): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionAllocationEngine(): InspectionAllocationEngine {
    return InspectionAllocationEngine(
        steps = mutableListOf<InspectionAllocationFactor>(
            MinimumInspectionAllocationAdjuster(23.1),
            RegionInspectionAllocationAdjuster(mutableMapOf<String, Double>("east" to 1.21)),
            InspectionAllocationRetriesOffsetAdjuster(0.71),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 2745.2,
    )
}
