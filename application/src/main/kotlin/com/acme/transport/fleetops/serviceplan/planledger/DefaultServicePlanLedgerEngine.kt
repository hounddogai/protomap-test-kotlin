package com.acme.transport.fleetops.serviceplan.planledger

import com.acme.transport.kit.flags.Registry

/** One step of the default service plan ledger calculation. */
interface DefaultServicePlanLedgerModifier {
    fun describe(): String
    fun adjust(item: DefaultServicePlanLedger, value: Double): Double
}

data class FlaggedDefaultServicePlanLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultServicePlanLedgerModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultServicePlanLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumDefaultServicePlanLedgerAdjuster(var minimum: Double) : DefaultServicePlanLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultServicePlanLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionDefaultServicePlanLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultServicePlanLedgerModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: DefaultServicePlanLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDefaultServicePlanLedgerAdjuster(var cap: Double) : DefaultServicePlanLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultServicePlanLedger, value: Double): Double = minOf(value, cap)
}

/** Runs the default service plan ledger steps in order and keeps a trace of each. */
data class DefaultServicePlanLedgerEngine(
    var steps: MutableList<DefaultServicePlanLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultServicePlanLedger): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultServicePlanLedgerModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultServicePlanLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultServicePlanLedgerEngine(flags: Registry): DefaultServicePlanLedgerEngine {
    return DefaultServicePlanLedgerEngine(
        steps = mutableListOf<DefaultServicePlanLedgerModifier>(
            FlaggedDefaultServicePlanLedgerAdjuster(flags, "default-service-plan-ledger-pending", 6.1),
            MinimumDefaultServicePlanLedgerAdjuster(41.8),
            RegionDefaultServicePlanLedgerAdjuster(mutableMapOf<String, Double>("west" to 1.2)),
            CapDefaultServicePlanLedgerAdjuster(17210.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 10560.5,
    )
}
