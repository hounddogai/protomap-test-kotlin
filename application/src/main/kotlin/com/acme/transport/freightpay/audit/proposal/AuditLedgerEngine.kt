package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.numbers.roundTo

/** One step of the audit ledger calculation. */
interface AuditLedgerStep {
    fun describe(): String
    fun adjust(item: AuditLedger, value: Double): Double
}

data class RoundingAuditLedgerAdjuster(var step: Double) : AuditLedgerStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditLedger, value: Double): Double = roundTo(value, step)
}

data class TieredAuditLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditLedgerStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAuditLedgerAdjuster(var percent: Double) : AuditLedgerStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AuditLedgerRatioOffsetAdjuster(var weight: Double) : AuditLedgerStep {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: AuditLedger, value: Double): Double = value + item.ratio * weight
}

/** Runs the audit ledger steps in order and keeps a trace of each. */
data class AuditLedgerEngine(
    var steps: MutableList<AuditLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AuditLedger): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditLedgerStep = steps[index]
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

    fun evaluateAll(items: MutableList<AuditLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAuditLedgerEngine(): AuditLedgerEngine {
    return AuditLedgerEngine(
        steps = mutableListOf<AuditLedgerStep>(
            RoundingAuditLedgerAdjuster(0.25),
            TieredAuditLedgerAdjuster(
                mutableListOf<Double>(3719.0, 3870.0, 5411.0, 8525.0),
                mutableListOf<Double>(0.9, 0.97, 1.07, 0.89),
            ),
            PercentAuditLedgerAdjuster(-21.9),
            AuditLedgerRatioOffsetAdjuster(1.56),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 46916.1,
    )
}
