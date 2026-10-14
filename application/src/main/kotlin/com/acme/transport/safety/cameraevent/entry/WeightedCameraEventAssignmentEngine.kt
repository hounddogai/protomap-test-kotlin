package com.acme.transport.safety.cameraevent.entry

/** One step of the weighted camera event assignment calculation. */
interface WeightedCameraEventAssignmentFactor {
    fun describe(): String
    fun adjust(item: WeightedCameraEventAssignment, value: Double): Double
}

data class CapWeightedCameraEventAssignmentAdjuster(var cap: Double) : WeightedCameraEventAssignmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedCameraEventAssignment, value: Double): Double = minOf(value, cap)
}

data class MinimumWeightedCameraEventAssignmentAdjuster(
    var minimum: Double,
) : WeightedCameraEventAssignmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedCameraEventAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentWeightedCameraEventAssignmentAdjuster(
    var percent: Double,
) : WeightedCameraEventAssignmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedCameraEventAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted camera event assignment steps in order and keeps a trace of each. */
data class WeightedCameraEventAssignmentEngine(
    var steps: MutableList<WeightedCameraEventAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedCameraEventAssignment): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedCameraEventAssignmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedCameraEventAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedCameraEventAssignmentEngine(): WeightedCameraEventAssignmentEngine {
    return WeightedCameraEventAssignmentEngine(
        steps = mutableListOf<WeightedCameraEventAssignmentFactor>(
            CapWeightedCameraEventAssignmentAdjuster(10406.0),
            MinimumWeightedCameraEventAssignmentAdjuster(21.9),
            PercentWeightedCameraEventAssignmentAdjuster(-11.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 97295.7,
    )
}
