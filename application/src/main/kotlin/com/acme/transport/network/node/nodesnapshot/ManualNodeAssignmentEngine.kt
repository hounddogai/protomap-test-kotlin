package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual node assignment calculation. */
interface ManualNodeAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: ManualNodeAssignment, value: Double): Double
}

data class FlaggedManualNodeAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualNodeAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualNodeAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ManualNodeAssignmentMarginOffsetAdjuster(var weight: Double) : ManualNodeAssignmentAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: ManualNodeAssignment, value: Double): Double = value + item.margin * weight
}

data class CapManualNodeAssignmentAdjuster(var cap: Double) : ManualNodeAssignmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualNodeAssignment, value: Double): Double = minOf(value, cap)
}

data class RoundingManualNodeAssignmentAdjuster(var step: Double) : ManualNodeAssignmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualNodeAssignment, value: Double): Double = roundTo(value, step)
}

/** Runs the manual node assignment steps in order and keeps a trace of each. */
data class ManualNodeAssignmentEngine(
    var steps: MutableList<ManualNodeAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualNodeAssignment): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualNodeAssignmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualNodeAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualNodeAssignmentEngine(flags: Registry): ManualNodeAssignmentEngine {
    return ManualNodeAssignmentEngine(
        steps = mutableListOf<ManualNodeAssignmentAdjuster>(
            FlaggedManualNodeAssignmentAdjuster(flags, "manual-node-assignment-default", 7.8),
            ManualNodeAssignmentMarginOffsetAdjuster(0.85),
            CapManualNodeAssignmentAdjuster(48251.0),
            RoundingManualNodeAssignmentAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 12286.8,
    )
}
