package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.kit.flags.Registry

/** One step of the inspection allocation calculation. */
interface InspectionAllocationModifier {
    fun describe(): String
    fun adjust(item: InspectionAllocation, value: Double): Double
}

data class FlaggedInspectionAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredInspectionAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class InspectionAllocationFloorValueOffsetAdjuster(var weight: Double) : InspectionAllocationModifier {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: InspectionAllocation, value: Double): Double = value + item.floorValue * weight
}

/** Runs the inspection allocation steps in order and keeps a trace of each. */
data class InspectionAllocationEngine(
    var steps: MutableList<InspectionAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: InspectionAllocation): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionAllocationModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultInspectionAllocationEngine(flags: Registry): InspectionAllocationEngine {
    return InspectionAllocationEngine(
        steps = mutableListOf<InspectionAllocationModifier>(
            FlaggedInspectionAllocationAdjuster(flags, "inspection-allocation-batched", 1.2),
            TieredInspectionAllocationAdjuster(
                mutableListOf<Double>(3356.0, 3600.0, 6947.0),
                mutableListOf<Double>(1.15, 1.07, 1.17),
            ),
            InspectionAllocationFloorValueOffsetAdjuster(1.33),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 44599.9,
    )
}
