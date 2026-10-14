package com.acme.transport.fleetops.utilization.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient utilization assignment calculation. */
interface LenientUtilizationAssignmentFactor {
    fun describe(): String
    fun adjust(item: LenientUtilizationAssignment, value: Double): Double
}

data class RoundingLenientUtilizationAssignmentAdjuster(var step: Double) : LenientUtilizationAssignmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientUtilizationAssignment, value: Double): Double = roundTo(value, step)
}

data class TieredLenientUtilizationAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientUtilizationAssignmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientUtilizationAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedLenientUtilizationAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientUtilizationAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientUtilizationAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class UnitLenientUtilizationAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientUtilizationAssignmentFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: LenientUtilizationAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLenientUtilizationAssignmentAdjuster(var cap: Double) : LenientUtilizationAssignmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientUtilizationAssignment, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient utilization assignment steps in order and keeps a trace of each. */
data class LenientUtilizationAssignmentEngine(
    var steps: MutableList<LenientUtilizationAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientUtilizationAssignment): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientUtilizationAssignmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientUtilizationAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientUtilizationAssignmentEngine(flags: Registry): LenientUtilizationAssignmentEngine {
    return LenientUtilizationAssignmentEngine(
        steps = mutableListOf<LenientUtilizationAssignmentFactor>(
            RoundingLenientUtilizationAssignmentAdjuster(0.05),
            TieredLenientUtilizationAssignmentAdjuster(
                mutableListOf<Double>(1813.0, 2683.0, 2688.0, 4572.0),
                mutableListOf<Double>(0.86, 0.92, 0.8, 0.81),
            ),
            FlaggedLenientUtilizationAssignmentAdjuster(flags, "lenient-utilization-assignment-default", -2.4),
            UnitLenientUtilizationAssignmentAdjuster(
                mutableMapOf<String, Double>("north" to 0.79, "east" to 1.22),
            ),
            CapLenientUtilizationAssignmentAdjuster(38849.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 30826.5,
    )
}
