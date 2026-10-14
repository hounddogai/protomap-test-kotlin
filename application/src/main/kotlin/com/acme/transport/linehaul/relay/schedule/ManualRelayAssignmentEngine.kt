package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.kit.flags.Registry

/** One step of the manual relay assignment calculation. */
interface ManualRelayAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: ManualRelayAssignment, value: Double): Double
}

data class MinimumManualRelayAssignmentAdjuster(var minimum: Double) : ManualRelayAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualRelayAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedManualRelayAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualRelayAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualRelayAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapManualRelayAssignmentAdjuster(var cap: Double) : ManualRelayAssignmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualRelayAssignment, value: Double): Double = minOf(value, cap)
}

data class PercentManualRelayAssignmentAdjuster(var percent: Double) : ManualRelayAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualRelayAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionManualRelayAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualRelayAssignmentAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: ManualRelayAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual relay assignment steps in order and keeps a trace of each. */
data class ManualRelayAssignmentEngine(
    var steps: MutableList<ManualRelayAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualRelayAssignment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualRelayAssignmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualRelayAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualRelayAssignmentEngine(flags: Registry): ManualRelayAssignmentEngine {
    return ManualRelayAssignmentEngine(
        steps = mutableListOf<ManualRelayAssignmentAdjuster>(
            MinimumManualRelayAssignmentAdjuster(44.3),
            FlaggedManualRelayAssignmentAdjuster(flags, "manual-relay-assignment-pending", -1.4),
            CapManualRelayAssignmentAdjuster(5705.0),
            PercentManualRelayAssignmentAdjuster(9.9),
            RegionManualRelayAssignmentAdjuster(mutableMapOf<String, Double>("north" to 1.08, "online" to 0.71)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 40905.7,
    )
}
