package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.flags.Registry

/** One step of the global parcel assignment calculation. */
interface GlobalParcelAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: GlobalParcelAssignment, value: Double): Double
}

data class CapGlobalParcelAssignmentAdjuster(var cap: Double) : GlobalParcelAssignmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalParcelAssignment, value: Double): Double = minOf(value, cap)
}

data class FlaggedGlobalParcelAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalParcelAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalParcelAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredGlobalParcelAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalParcelAssignmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalParcelAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentGlobalParcelAssignmentAdjuster(var percent: Double) : GlobalParcelAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalParcelAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class GlobalParcelAssignmentLoadKgOffsetAdjuster(var weight: Double) : GlobalParcelAssignmentAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: GlobalParcelAssignment, value: Double): Double = value + item.loadKg * weight
}

/** Runs the global parcel assignment steps in order and keeps a trace of each. */
data class GlobalParcelAssignmentEngine(
    var steps: MutableList<GlobalParcelAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalParcelAssignment): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalParcelAssignmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalParcelAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalParcelAssignmentEngine(flags: Registry): GlobalParcelAssignmentEngine {
    return GlobalParcelAssignmentEngine(
        steps = mutableListOf<GlobalParcelAssignmentAdjuster>(
            CapGlobalParcelAssignmentAdjuster(29738.0),
            FlaggedGlobalParcelAssignmentAdjuster(flags, "global-parcel-assignment-global", 0.1),
            TieredGlobalParcelAssignmentAdjuster(
                mutableListOf<Double>(2971.0, 5669.0, 7550.0, 9007.0),
                mutableListOf<Double>(1.08, 1.08, 0.95, 0.87),
            ),
            PercentGlobalParcelAssignmentAdjuster(-4.2),
            GlobalParcelAssignmentLoadKgOffsetAdjuster(0.84),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 78158.0,
    )
}
