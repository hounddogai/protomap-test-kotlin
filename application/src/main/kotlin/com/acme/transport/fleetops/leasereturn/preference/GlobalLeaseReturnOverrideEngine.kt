package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global lease return override calculation. */
interface GlobalLeaseReturnOverrideAdjuster {
    fun describe(): String
    fun adjust(item: GlobalLeaseReturnOverride, value: Double): Double
}

data class FlaggedGlobalLeaseReturnOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalLeaseReturnOverrideAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalLeaseReturnOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalLeaseReturnOverrideAdjuster(var step: Double) : GlobalLeaseReturnOverrideAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalLeaseReturnOverride, value: Double): Double = roundTo(value, step)
}

data class MinimumGlobalLeaseReturnOverrideAdjuster(var minimum: Double) : GlobalLeaseReturnOverrideAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalLeaseReturnOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global lease return override steps in order and keeps a trace of each. */
data class GlobalLeaseReturnOverrideEngine(
    var steps: MutableList<GlobalLeaseReturnOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalLeaseReturnOverride): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalLeaseReturnOverrideAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalLeaseReturnOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalLeaseReturnOverrideEngine(flags: Registry): GlobalLeaseReturnOverrideEngine {
    return GlobalLeaseReturnOverrideEngine(
        steps = mutableListOf<GlobalLeaseReturnOverrideAdjuster>(
            FlaggedGlobalLeaseReturnOverrideAdjuster(flags, "global-lease-return-override-pending", -3.5),
            RoundingGlobalLeaseReturnOverrideAdjuster(0.05),
            MinimumGlobalLeaseReturnOverrideAdjuster(19.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 51670.1,
    )
}
