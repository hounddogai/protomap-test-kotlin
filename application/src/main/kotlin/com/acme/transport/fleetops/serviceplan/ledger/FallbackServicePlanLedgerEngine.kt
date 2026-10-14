package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.flags.Registry

/** One step of the fallback service plan ledger calculation. */
interface FallbackServicePlanLedgerAdjuster {
    fun describe(): String
    fun adjust(item: FallbackServicePlanLedger, value: Double): Double
}

data class CapFallbackServicePlanLedgerAdjuster(var cap: Double) : FallbackServicePlanLedgerAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackServicePlanLedger, value: Double): Double = minOf(value, cap)
}

data class FallbackServicePlanLedgerLoadKgOffsetAdjuster(
    var weight: Double,
) : FallbackServicePlanLedgerAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: FallbackServicePlanLedger, value: Double): Double = value + item.loadKg * weight
}

data class PercentFallbackServicePlanLedgerAdjuster(var percent: Double) : FallbackServicePlanLedgerAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackServicePlanLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class UnitFallbackServicePlanLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackServicePlanLedgerAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: FallbackServicePlanLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFallbackServicePlanLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackServicePlanLedgerAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackServicePlanLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFallbackServicePlanLedgerAdjuster(var minimum: Double) : FallbackServicePlanLedgerAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackServicePlanLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback service plan ledger steps in order and keeps a trace of each. */
data class FallbackServicePlanLedgerEngine(
    var steps: MutableList<FallbackServicePlanLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackServicePlanLedger): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackServicePlanLedgerAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackServicePlanLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackServicePlanLedgerEngine(flags: Registry): FallbackServicePlanLedgerEngine {
    return FallbackServicePlanLedgerEngine(
        steps = mutableListOf<FallbackServicePlanLedgerAdjuster>(
            CapFallbackServicePlanLedgerAdjuster(20130.0),
            FallbackServicePlanLedgerLoadKgOffsetAdjuster(1.1),
            PercentFallbackServicePlanLedgerAdjuster(-10.5),
            UnitFallbackServicePlanLedgerAdjuster(mutableMapOf<String, Double>("central" to 1.19)),
            FlaggedFallbackServicePlanLedgerAdjuster(flags, "fallback-service-plan-ledger-capped", -2.3),
            MinimumFallbackServicePlanLedgerAdjuster(30.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 79858.1,
    )
}
