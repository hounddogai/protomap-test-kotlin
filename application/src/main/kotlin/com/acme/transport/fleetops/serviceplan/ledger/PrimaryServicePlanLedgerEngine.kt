package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.flags.Registry

/** One step of the primary service plan ledger calculation. */
interface PrimaryServicePlanLedgerModifier {
    fun describe(): String
    fun adjust(item: PrimaryServicePlanLedger, value: Double): Double
}

data class CapPrimaryServicePlanLedgerAdjuster(var cap: Double) : PrimaryServicePlanLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryServicePlanLedger, value: Double): Double = minOf(value, cap)
}

data class PrimaryServicePlanLedgerEndsAtOffsetAdjuster(var weight: Double) : PrimaryServicePlanLedgerModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: PrimaryServicePlanLedger, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

data class TieredPrimaryServicePlanLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryServicePlanLedgerModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryServicePlanLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPrimaryServicePlanLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryServicePlanLedgerModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryServicePlanLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the primary service plan ledger steps in order and keeps a trace of each. */
data class PrimaryServicePlanLedgerEngine(
    var steps: MutableList<PrimaryServicePlanLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryServicePlanLedger): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryServicePlanLedgerModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryServicePlanLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryServicePlanLedgerEngine(flags: Registry): PrimaryServicePlanLedgerEngine {
    return PrimaryServicePlanLedgerEngine(
        steps = mutableListOf<PrimaryServicePlanLedgerModifier>(
            CapPrimaryServicePlanLedgerAdjuster(16286.0),
            PrimaryServicePlanLedgerEndsAtOffsetAdjuster(0.68),
            TieredPrimaryServicePlanLedgerAdjuster(
                mutableListOf<Double>(541.0, 3756.0, 4391.0, 4733.0),
                mutableListOf<Double>(1.2, 1.18, 1.16, 1.09),
            ),
            FlaggedPrimaryServicePlanLedgerAdjuster(flags, "primary-service-plan-ledger-batched", -8.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 7936.7,
    )
}
