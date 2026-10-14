package com.acme.transport.fleetops.serviceplan.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending service plan ledger calculation. */
interface PendingServicePlanLedgerStep {
    fun describe(): String
    fun adjust(item: PendingServicePlanLedger, value: Double): Double
}

data class PercentPendingServicePlanLedgerAdjuster(var percent: Double) : PendingServicePlanLedgerStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingServicePlanLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingServicePlanLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingServicePlanLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingServicePlanLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPendingServicePlanLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingServicePlanLedgerStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingServicePlanLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapPendingServicePlanLedgerAdjuster(var cap: Double) : PendingServicePlanLedgerStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingServicePlanLedger, value: Double): Double = minOf(value, cap)
}

data class LocalePendingServicePlanLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingServicePlanLedgerStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: PendingServicePlanLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingPendingServicePlanLedgerAdjuster(var step: Double) : PendingServicePlanLedgerStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingServicePlanLedger, value: Double): Double = roundTo(value, step)
}

/** Runs the pending service plan ledger steps in order and keeps a trace of each. */
data class PendingServicePlanLedgerEngine(
    var steps: MutableList<PendingServicePlanLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingServicePlanLedger): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingServicePlanLedgerStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingServicePlanLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingServicePlanLedgerEngine(flags: Registry): PendingServicePlanLedgerEngine {
    return PendingServicePlanLedgerEngine(
        steps = mutableListOf<PendingServicePlanLedgerStep>(
            PercentPendingServicePlanLedgerAdjuster(-21.9),
            FlaggedPendingServicePlanLedgerAdjuster(flags, "pending-service-plan-ledger-batched", -3.9),
            TieredPendingServicePlanLedgerAdjuster(
                mutableListOf<Double>(677.0, 685.0, 1059.0, 4443.0),
                mutableListOf<Double>(0.98, 1.08, 1.17, 1.2),
            ),
            CapPendingServicePlanLedgerAdjuster(26433.0),
            LocalePendingServicePlanLedgerAdjuster(mutableMapOf<String, Double>("south" to 1.37)),
            RoundingPendingServicePlanLedgerAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 86475.5,
    )
}
