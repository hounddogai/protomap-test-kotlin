package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.kit.flags.Registry

/** One step of the regional redelivery assignment calculation. */
interface RegionalRedeliveryAssignmentStep {
    fun describe(): String
    fun adjust(item: RegionalRedeliveryAssignment, value: Double): Double
}

data class RegionalRedeliveryAssignmentFuelLitersOffsetAdjuster(
    var weight: Double,
) : RegionalRedeliveryAssignmentStep {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: RegionalRedeliveryAssignment, value: Double): Double = value + item.fuelLiters * weight
}

data class UnitRegionalRedeliveryAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalRedeliveryAssignmentStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: RegionalRedeliveryAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRegionalRedeliveryAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalRedeliveryAssignmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalRedeliveryAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRegionalRedeliveryAssignmentAdjuster(var percent: Double) : RegionalRedeliveryAssignmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalRedeliveryAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRegionalRedeliveryAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalRedeliveryAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalRedeliveryAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional redelivery assignment steps in order and keeps a trace of each. */
data class RegionalRedeliveryAssignmentEngine(
    var steps: MutableList<RegionalRedeliveryAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalRedeliveryAssignment): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalRedeliveryAssignmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalRedeliveryAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalRedeliveryAssignmentEngine(flags: Registry): RegionalRedeliveryAssignmentEngine {
    return RegionalRedeliveryAssignmentEngine(
        steps = mutableListOf<RegionalRedeliveryAssignmentStep>(
            RegionalRedeliveryAssignmentFuelLitersOffsetAdjuster(1.25),
            UnitRegionalRedeliveryAssignmentAdjuster(
                mutableMapOf<String, Double>("retail" to 0.77, "east" to 1.2),
            ),
            TieredRegionalRedeliveryAssignmentAdjuster(
                mutableListOf<Double>(3595.0, 5645.0),
                mutableListOf<Double>(0.99, 0.93),
            ),
            PercentRegionalRedeliveryAssignmentAdjuster(-21.7),
            FlaggedRegionalRedeliveryAssignmentAdjuster(flags, "regional-redelivery-assignment-weighted", 9.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 3018.9,
    )
}
