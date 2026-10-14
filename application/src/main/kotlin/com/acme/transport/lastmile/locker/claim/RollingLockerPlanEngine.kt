package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.flags.Registry

/** One step of the rolling locker plan calculation. */
interface RollingLockerPlanAdjuster {
    fun describe(): String
    fun adjust(item: RollingLockerPlan, value: Double): Double
}

data class FlaggedRollingLockerPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingLockerPlanAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingLockerPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRollingLockerPlanAdjuster(var cap: Double) : RollingLockerPlanAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingLockerPlan, value: Double): Double = minOf(value, cap)
}

data class RollingLockerPlanBatchSizeOffsetAdjuster(var weight: Double) : RollingLockerPlanAdjuster {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: RollingLockerPlan, value: Double): Double = value + item.batchSize.toDouble() * weight
}

data class MinimumRollingLockerPlanAdjuster(var minimum: Double) : RollingLockerPlanAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingLockerPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling locker plan steps in order and keeps a trace of each. */
data class RollingLockerPlanEngine(
    var steps: MutableList<RollingLockerPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingLockerPlan): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingLockerPlanAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingLockerPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingLockerPlanEngine(flags: Registry): RollingLockerPlanEngine {
    return RollingLockerPlanEngine(
        steps = mutableListOf<RollingLockerPlanAdjuster>(
            FlaggedRollingLockerPlanAdjuster(flags, "rolling-locker-plan-strict", -5.1),
            CapRollingLockerPlanAdjuster(47109.0),
            RollingLockerPlanBatchSizeOffsetAdjuster(0.96),
            MinimumRollingLockerPlanAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 42810.1,
    )
}
