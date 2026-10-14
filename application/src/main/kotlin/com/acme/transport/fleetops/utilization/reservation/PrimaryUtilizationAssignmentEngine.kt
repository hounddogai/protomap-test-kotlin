package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary utilization assignment calculation. */
interface PrimaryUtilizationAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryUtilizationAssignment, value: Double): Double
}

data class MinimumPrimaryUtilizationAssignmentAdjuster(
    var minimum: Double,
) : PrimaryUtilizationAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryUtilizationAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPrimaryUtilizationAssignmentAdjuster(
    var step: Double,
) : PrimaryUtilizationAssignmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryUtilizationAssignment, value: Double): Double = roundTo(value, step)
}

data class FlaggedPrimaryUtilizationAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryUtilizationAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryUtilizationAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the primary utilization assignment steps in order and keeps a trace of each. */
data class PrimaryUtilizationAssignmentEngine(
    var steps: MutableList<PrimaryUtilizationAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryUtilizationAssignment): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryUtilizationAssignmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryUtilizationAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryUtilizationAssignmentEngine(flags: Registry): PrimaryUtilizationAssignmentEngine {
    return PrimaryUtilizationAssignmentEngine(
        steps = mutableListOf<PrimaryUtilizationAssignmentAdjuster>(
            MinimumPrimaryUtilizationAssignmentAdjuster(10.9),
            RoundingPrimaryUtilizationAssignmentAdjuster(5.0),
            FlaggedPrimaryUtilizationAssignmentAdjuster(flags, "primary-utilization-assignment-pending", -4.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 73122.4,
    )
}
