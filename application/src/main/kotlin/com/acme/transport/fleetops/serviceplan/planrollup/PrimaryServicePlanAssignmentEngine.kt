package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary service plan assignment calculation. */
interface PrimaryServicePlanAssignmentFactor {
    fun describe(): String
    fun adjust(item: PrimaryServicePlanAssignment, value: Double): Double
}

data class CapPrimaryServicePlanAssignmentAdjuster(var cap: Double) : PrimaryServicePlanAssignmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryServicePlanAssignment, value: Double): Double = minOf(value, cap)
}

data class TieredPrimaryServicePlanAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryServicePlanAssignmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryServicePlanAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPrimaryServicePlanAssignmentAdjuster(var step: Double) : PrimaryServicePlanAssignmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryServicePlanAssignment, value: Double): Double = roundTo(value, step)
}

data class FlaggedPrimaryServicePlanAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryServicePlanAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryServicePlanAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CountryPrimaryServicePlanAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryServicePlanAssignmentFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: PrimaryServicePlanAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary service plan assignment steps in order and keeps a trace of each. */
data class PrimaryServicePlanAssignmentEngine(
    var steps: MutableList<PrimaryServicePlanAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryServicePlanAssignment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryServicePlanAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryServicePlanAssignmentEngine(flags: Registry): PrimaryServicePlanAssignmentEngine {
    return PrimaryServicePlanAssignmentEngine(
        steps = mutableListOf<PrimaryServicePlanAssignmentFactor>(
            CapPrimaryServicePlanAssignmentAdjuster(12009.0),
            TieredPrimaryServicePlanAssignmentAdjuster(
                mutableListOf<Double>(2925.0, 5637.0, 6443.0, 7910.0),
                mutableListOf<Double>(0.88, 0.93, 1.0, 1.06),
            ),
            RoundingPrimaryServicePlanAssignmentAdjuster(0.05),
            FlaggedPrimaryServicePlanAssignmentAdjuster(
                flags,
                "primary-service-plan-assignment-scheduled",
                -5.3,
            ),
            CountryPrimaryServicePlanAssignmentAdjuster(
                mutableMapOf<String, Double>("central" to 0.93, "east" to 0.78, "south" to 1.29),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 56391.4,
    )
}
