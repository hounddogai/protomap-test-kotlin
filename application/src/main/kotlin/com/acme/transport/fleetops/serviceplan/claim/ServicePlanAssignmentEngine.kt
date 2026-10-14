package com.acme.transport.fleetops.serviceplan.claim

import com.acme.transport.kit.numbers.roundTo

/** One step of the service plan assignment calculation. */
interface ServicePlanAssignmentModifier {
    fun describe(): String
    fun adjust(item: ServicePlanAssignment, value: Double): Double
}

data class CapServicePlanAssignmentAdjuster(var cap: Double) : ServicePlanAssignmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServicePlanAssignment, value: Double): Double = minOf(value, cap)
}

data class RoundingServicePlanAssignmentAdjuster(var step: Double) : ServicePlanAssignmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanAssignment, value: Double): Double = roundTo(value, step)
}

data class TieredServicePlanAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanAssignmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentServicePlanAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ServicePlanAssignmentModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: ServicePlanAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumServicePlanAssignmentAdjuster(var minimum: Double) : ServicePlanAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServicePlanAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ServicePlanAssignmentAttemptsOffsetAdjuster(var weight: Double) : ServicePlanAssignmentModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: ServicePlanAssignment, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

/** Runs the service plan assignment steps in order and keeps a trace of each. */
data class ServicePlanAssignmentEngine(
    var steps: MutableList<ServicePlanAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ServicePlanAssignment): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanAssignmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultServicePlanAssignmentEngine(): ServicePlanAssignmentEngine {
    return ServicePlanAssignmentEngine(
        steps = mutableListOf<ServicePlanAssignmentModifier>(
            CapServicePlanAssignmentAdjuster(29418.0),
            RoundingServicePlanAssignmentAdjuster(0.25),
            TieredServicePlanAssignmentAdjuster(
                mutableListOf<Double>(1664.0, 6292.0, 7563.0, 9755.0),
                mutableListOf<Double>(1.08, 0.97, 0.84, 0.87),
            ),
            SegmentServicePlanAssignmentAdjuster(mutableMapOf<String, Double>("west" to 0.91)),
            MinimumServicePlanAssignmentAdjuster(47.1),
            ServicePlanAssignmentAttemptsOffsetAdjuster(1.42),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 17609.1,
    )
}
