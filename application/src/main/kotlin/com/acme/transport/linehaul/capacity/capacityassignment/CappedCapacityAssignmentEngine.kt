package com.acme.transport.linehaul.capacity.capacityassignment

/** One step of the capped capacity assignment calculation. */
interface CappedCapacityAssignmentStep {
    fun describe(): String
    fun adjust(item: CappedCapacityAssignment, value: Double): Double
}

data class CappedCapacityAssignmentWindowDaysOffsetAdjuster(var weight: Double) : CappedCapacityAssignmentStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: CappedCapacityAssignment, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class CapCappedCapacityAssignmentAdjuster(var cap: Double) : CappedCapacityAssignmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedCapacityAssignment, value: Double): Double = minOf(value, cap)
}

data class MinimumCappedCapacityAssignmentAdjuster(var minimum: Double) : CappedCapacityAssignmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedCapacityAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CategoryCappedCapacityAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedCapacityAssignmentStep {
    override fun describe(): String = "by category"

    override fun adjust(item: CappedCapacityAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped capacity assignment steps in order and keeps a trace of each. */
data class CappedCapacityAssignmentEngine(
    var steps: MutableList<CappedCapacityAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedCapacityAssignment): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedCapacityAssignmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedCapacityAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedCapacityAssignmentEngine(): CappedCapacityAssignmentEngine {
    return CappedCapacityAssignmentEngine(
        steps = mutableListOf<CappedCapacityAssignmentStep>(
            CappedCapacityAssignmentWindowDaysOffsetAdjuster(0.59),
            CapCappedCapacityAssignmentAdjuster(47167.0),
            MinimumCappedCapacityAssignmentAdjuster(13.8),
            CategoryCappedCapacityAssignmentAdjuster(
                mutableMapOf<String, Double>("online" to 1.26, "south" to 0.8, "central" to 1.21),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 52763.1,
    )
}
