package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.flags.Registry

/** One step of the pending service plan target calculation. */
interface PendingServicePlanTargetFactor {
    fun describe(): String
    fun adjust(item: PendingServicePlanTarget, value: Double): Double
}

data class FlaggedPendingServicePlanTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingServicePlanTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingServicePlanTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPendingServicePlanTargetAdjuster(var cap: Double) : PendingServicePlanTargetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingServicePlanTarget, value: Double): Double = minOf(value, cap)
}

data class PercentPendingServicePlanTargetAdjuster(var percent: Double) : PendingServicePlanTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingServicePlanTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending service plan target steps in order and keeps a trace of each. */
data class PendingServicePlanTargetEngine(
    var steps: MutableList<PendingServicePlanTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingServicePlanTarget): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingServicePlanTargetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingServicePlanTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingServicePlanTargetEngine(flags: Registry): PendingServicePlanTargetEngine {
    return PendingServicePlanTargetEngine(
        steps = mutableListOf<PendingServicePlanTargetFactor>(
            FlaggedPendingServicePlanTargetAdjuster(flags, "pending-service-plan-target-manual", -9.4),
            CapPendingServicePlanTargetAdjuster(22664.0),
            PercentPendingServicePlanTargetAdjuster(3.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 14818.3,
    )
}
