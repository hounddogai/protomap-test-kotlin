package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global lease return assignment calculation. */
interface GlobalLeaseReturnAssignmentStep {
    fun describe(): String
    fun adjust(item: GlobalLeaseReturnAssignment, value: Double): Double
}

data class PercentGlobalLeaseReturnAssignmentAdjuster(var percent: Double) : GlobalLeaseReturnAssignmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalLeaseReturnAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumGlobalLeaseReturnAssignmentAdjuster(var minimum: Double) : GlobalLeaseReturnAssignmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalLeaseReturnAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedGlobalLeaseReturnAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalLeaseReturnAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalLeaseReturnAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapGlobalLeaseReturnAssignmentAdjuster(var cap: Double) : GlobalLeaseReturnAssignmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalLeaseReturnAssignment, value: Double): Double = minOf(value, cap)
}

/** Runs the global lease return assignment steps in order and keeps a trace of each. */
data class GlobalLeaseReturnAssignmentEngine(
    var steps: MutableList<GlobalLeaseReturnAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalLeaseReturnAssignment): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalLeaseReturnAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalLeaseReturnAssignmentEngine(flags: Registry): GlobalLeaseReturnAssignmentEngine {
    return GlobalLeaseReturnAssignmentEngine(
        steps = mutableListOf<GlobalLeaseReturnAssignmentStep>(
            PercentGlobalLeaseReturnAssignmentAdjuster(1.4),
            MinimumGlobalLeaseReturnAssignmentAdjuster(28.1),
            FlaggedGlobalLeaseReturnAssignmentAdjuster(flags, "global-lease-return-assignment-fallback", -2.9),
            CapGlobalLeaseReturnAssignmentAdjuster(37966.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 30455.5,
    )
}
