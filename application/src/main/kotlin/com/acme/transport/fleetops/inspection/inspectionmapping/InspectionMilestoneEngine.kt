package com.acme.transport.fleetops.inspection.inspectionmapping

/** One step of the inspection milestone calculation. */
interface InspectionMilestoneFactor {
    fun describe(): String
    fun adjust(item: InspectionMilestone, value: Double): Double
}

data class CapInspectionMilestoneAdjuster(var cap: Double) : InspectionMilestoneFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionMilestone, value: Double): Double = minOf(value, cap)
}

data class InspectionMilestoneOdometerOffsetAdjuster(var weight: Double) : InspectionMilestoneFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: InspectionMilestone, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class MinimumInspectionMilestoneAdjuster(var minimum: Double) : InspectionMilestoneFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the inspection milestone steps in order and keeps a trace of each. */
data class InspectionMilestoneEngine(
    var steps: MutableList<InspectionMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionMilestone): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionMilestoneFactor = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionMilestoneEngine(): InspectionMilestoneEngine {
    return InspectionMilestoneEngine(
        steps = mutableListOf<InspectionMilestoneFactor>(
            CapInspectionMilestoneAdjuster(48231.0),
            InspectionMilestoneOdometerOffsetAdjuster(0.5),
            MinimumInspectionMilestoneAdjuster(47.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 57122.0,
    )
}
