package com.acme.transport.fleetops.leasereturn.returnrequest

import com.acme.transport.kit.flags.Registry

/** One step of the legacy lease return request calculation. */
interface LegacyLeaseReturnRequestStep {
    fun describe(): String
    fun adjust(item: LegacyLeaseReturnRequest, value: Double): Double
}

data class MinimumLegacyLeaseReturnRequestAdjuster(var minimum: Double) : LegacyLeaseReturnRequestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyLeaseReturnRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLegacyLeaseReturnRequestAdjuster(var cap: Double) : LegacyLeaseReturnRequestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyLeaseReturnRequest, value: Double): Double = minOf(value, cap)
}

data class FlaggedLegacyLeaseReturnRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyLeaseReturnRequestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyLeaseReturnRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLegacyLeaseReturnRequestAdjuster(var percent: Double) : LegacyLeaseReturnRequestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyLeaseReturnRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy lease return request steps in order and keeps a trace of each. */
data class LegacyLeaseReturnRequestEngine(
    var steps: MutableList<LegacyLeaseReturnRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyLeaseReturnRequest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyLeaseReturnRequestStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyLeaseReturnRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyLeaseReturnRequestEngine(flags: Registry): LegacyLeaseReturnRequestEngine {
    return LegacyLeaseReturnRequestEngine(
        steps = mutableListOf<LegacyLeaseReturnRequestStep>(
            MinimumLegacyLeaseReturnRequestAdjuster(41.7),
            CapLegacyLeaseReturnRequestAdjuster(33747.0),
            FlaggedLegacyLeaseReturnRequestAdjuster(flags, "legacy-lease-return-request-lenient", -1.5),
            PercentLegacyLeaseReturnRequestAdjuster(-3.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 98409.1,
    )
}
