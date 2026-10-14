package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending inspection allocation calculation. */
interface PendingInspectionAllocationModifier {
    fun describe(): String
    fun adjust(item: PendingInspectionAllocation, value: Double): Double
}

data class CapPendingInspectionAllocationAdjuster(var cap: Double) : PendingInspectionAllocationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingInspectionAllocation, value: Double): Double = minOf(value, cap)
}

data class RoundingPendingInspectionAllocationAdjuster(var step: Double) : PendingInspectionAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingInspectionAllocation, value: Double): Double = roundTo(value, step)
}

data class TieredPendingInspectionAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingInspectionAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingInspectionAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPendingInspectionAllocationAdjuster(
    var percent: Double,
) : PendingInspectionAllocationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingInspectionAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPendingInspectionAllocationAdjuster(
    var minimum: Double,
) : PendingInspectionAllocationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingInspectionAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotPendingInspectionAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingInspectionAllocationModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: PendingInspectionAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending inspection allocation steps in order and keeps a trace of each. */
data class PendingInspectionAllocationEngine(
    var steps: MutableList<PendingInspectionAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingInspectionAllocation): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingInspectionAllocationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingInspectionAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingInspectionAllocationEngine(): PendingInspectionAllocationEngine {
    return PendingInspectionAllocationEngine(
        steps = mutableListOf<PendingInspectionAllocationModifier>(
            CapPendingInspectionAllocationAdjuster(18148.0),
            RoundingPendingInspectionAllocationAdjuster(0.5),
            TieredPendingInspectionAllocationAdjuster(
                mutableListOf<Double>(6411.0, 7641.0),
                mutableListOf<Double>(1.15, 1.17),
            ),
            PercentPendingInspectionAllocationAdjuster(-18.0),
            MinimumPendingInspectionAllocationAdjuster(41.0),
            DepotPendingInspectionAllocationAdjuster(
                mutableMapOf<String, Double>("south" to 1.01, "east" to 1.31, "north" to 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 11646.2,
    )
}
