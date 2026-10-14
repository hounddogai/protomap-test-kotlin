package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.flags.Registry

/** One step of the default parcel assignment calculation. */
interface DefaultParcelAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: DefaultParcelAssignment, value: Double): Double
}

data class PercentDefaultParcelAssignmentAdjuster(var percent: Double) : DefaultParcelAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultParcelAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DefaultParcelAssignmentMinimumOffsetAdjuster(var weight: Double) : DefaultParcelAssignmentAdjuster {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: DefaultParcelAssignment, value: Double): Double = value + item.minimum * weight
}

data class FlaggedDefaultParcelAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultParcelAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultParcelAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefaultParcelAssignmentAdjuster(var cap: Double) : DefaultParcelAssignmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultParcelAssignment, value: Double): Double = minOf(value, cap)
}

data class CountryDefaultParcelAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultParcelAssignmentAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: DefaultParcelAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDefaultParcelAssignmentAdjuster(var minimum: Double) : DefaultParcelAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultParcelAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default parcel assignment steps in order and keeps a trace of each. */
data class DefaultParcelAssignmentEngine(
    var steps: MutableList<DefaultParcelAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultParcelAssignment): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultParcelAssignmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultParcelAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultParcelAssignmentEngine(flags: Registry): DefaultParcelAssignmentEngine {
    return DefaultParcelAssignmentEngine(
        steps = mutableListOf<DefaultParcelAssignmentAdjuster>(
            PercentDefaultParcelAssignmentAdjuster(-17.5),
            DefaultParcelAssignmentMinimumOffsetAdjuster(0.83),
            FlaggedDefaultParcelAssignmentAdjuster(flags, "default-parcel-assignment-strict", 0.1),
            CapDefaultParcelAssignmentAdjuster(20162.0),
            CountryDefaultParcelAssignmentAdjuster(mutableMapOf<String, Double>("central" to 1.07)),
            MinimumDefaultParcelAssignmentAdjuster(14.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 89328.0,
    )
}
