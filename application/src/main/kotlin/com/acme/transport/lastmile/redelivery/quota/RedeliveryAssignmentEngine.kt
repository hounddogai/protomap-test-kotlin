package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the redelivery assignment calculation. */
interface RedeliveryAssignmentStep {
    fun describe(): String
    fun adjust(item: RedeliveryAssignment, value: Double): Double
}

data class CapRedeliveryAssignmentAdjuster(var cap: Double) : RedeliveryAssignmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryAssignment, value: Double): Double = minOf(value, cap)
}

data class MinimumRedeliveryAssignmentAdjuster(var minimum: Double) : RedeliveryAssignmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRedeliveryAssignmentAdjuster(var percent: Double) : RedeliveryAssignmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRedeliveryAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RedeliveryAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RedeliveryAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRedeliveryAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RedeliveryAssignmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RedeliveryAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the redelivery assignment steps in order and keeps a trace of each. */
data class RedeliveryAssignmentEngine(
    var steps: MutableList<RedeliveryAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RedeliveryAssignment): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RedeliveryAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRedeliveryAssignmentEngine(flags: Registry): RedeliveryAssignmentEngine {
    return RedeliveryAssignmentEngine(
        steps = mutableListOf<RedeliveryAssignmentStep>(
            CapRedeliveryAssignmentAdjuster(35952.0),
            MinimumRedeliveryAssignmentAdjuster(31.3),
            PercentRedeliveryAssignmentAdjuster(11.0),
            FlaggedRedeliveryAssignmentAdjuster(flags, "redelivery-assignment-pending", 3.7),
            TieredRedeliveryAssignmentAdjuster(
                mutableListOf<Double>(2567.0, 7327.0, 8123.0, 9049.0),
                mutableListOf<Double>(1.18, 0.89, 1.04, 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 97311.3,
    )
}
