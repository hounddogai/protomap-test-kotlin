package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending lease return preference calculation. */
interface PendingLeaseReturnPreferenceStep {
    fun describe(): String
    fun adjust(item: PendingLeaseReturnPreference, value: Double): Double
}

data class PendingLeaseReturnPreferenceBudgetOffsetAdjuster(
    var weight: Double,
) : PendingLeaseReturnPreferenceStep {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: PendingLeaseReturnPreference, value: Double): Double = value + item.budget * weight
}

data class MinimumPendingLeaseReturnPreferenceAdjuster(var minimum: Double) : PendingLeaseReturnPreferenceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingLeaseReturnPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPendingLeaseReturnPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingLeaseReturnPreferenceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingLeaseReturnPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPendingLeaseReturnPreferenceAdjuster(var step: Double) : PendingLeaseReturnPreferenceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingLeaseReturnPreference, value: Double): Double = roundTo(value, step)
}

data class CapPendingLeaseReturnPreferenceAdjuster(var cap: Double) : PendingLeaseReturnPreferenceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingLeaseReturnPreference, value: Double): Double = minOf(value, cap)
}

/** Runs the pending lease return preference steps in order and keeps a trace of each. */
data class PendingLeaseReturnPreferenceEngine(
    var steps: MutableList<PendingLeaseReturnPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingLeaseReturnPreference): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingLeaseReturnPreferenceStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingLeaseReturnPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingLeaseReturnPreferenceEngine(flags: Registry): PendingLeaseReturnPreferenceEngine {
    return PendingLeaseReturnPreferenceEngine(
        steps = mutableListOf<PendingLeaseReturnPreferenceStep>(
            PendingLeaseReturnPreferenceBudgetOffsetAdjuster(1.74),
            MinimumPendingLeaseReturnPreferenceAdjuster(48.0),
            FlaggedPendingLeaseReturnPreferenceAdjuster(flags, "pending-lease-return-preference-rolling", 2.6),
            RoundingPendingLeaseReturnPreferenceAdjuster(0.5),
            CapPendingLeaseReturnPreferenceAdjuster(26921.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 48437.4,
    )
}
