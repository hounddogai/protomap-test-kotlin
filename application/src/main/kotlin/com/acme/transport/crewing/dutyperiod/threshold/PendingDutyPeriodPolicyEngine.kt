package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending duty period policy calculation. */
interface PendingDutyPeriodPolicyStep {
    fun describe(): String
    fun adjust(item: PendingDutyPeriodPolicy, value: Double): Double
}

data class MinimumPendingDutyPeriodPolicyAdjuster(var minimum: Double) : PendingDutyPeriodPolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingDutyPeriodPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPendingDutyPeriodPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingDutyPeriodPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingDutyPeriodPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPendingDutyPeriodPolicyAdjuster(var step: Double) : PendingDutyPeriodPolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingDutyPeriodPolicy, value: Double): Double = roundTo(value, step)
}

/** Runs the pending duty period policy steps in order and keeps a trace of each. */
data class PendingDutyPeriodPolicyEngine(
    var steps: MutableList<PendingDutyPeriodPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingDutyPeriodPolicy): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingDutyPeriodPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingDutyPeriodPolicyEngine(flags: Registry): PendingDutyPeriodPolicyEngine {
    return PendingDutyPeriodPolicyEngine(
        steps = mutableListOf<PendingDutyPeriodPolicyStep>(
            MinimumPendingDutyPeriodPolicyAdjuster(13.1),
            FlaggedPendingDutyPeriodPolicyAdjuster(flags, "pending-duty-period-policy-default", -9.2),
            RoundingPendingDutyPeriodPolicyAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 69989.9,
    )
}
