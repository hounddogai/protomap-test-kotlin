package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.kit.flags.Registry

/** One step of the pending dwell ledger calculation. */
interface PendingDwellLedgerModifier {
    fun describe(): String
    fun adjust(item: PendingDwellLedger, value: Double): Double
}

data class FlaggedPendingDwellLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingDwellLedgerModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingDwellLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPendingDwellLedgerAdjuster(var cap: Double) : PendingDwellLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingDwellLedger, value: Double): Double = minOf(value, cap)
}

data class TieredPendingDwellLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingDwellLedgerModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingDwellLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending dwell ledger steps in order and keeps a trace of each. */
data class PendingDwellLedgerEngine(
    var steps: MutableList<PendingDwellLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingDwellLedger): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingDwellLedgerModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingDwellLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingDwellLedgerEngine(flags: Registry): PendingDwellLedgerEngine {
    return PendingDwellLedgerEngine(
        steps = mutableListOf<PendingDwellLedgerModifier>(
            FlaggedPendingDwellLedgerAdjuster(flags, "pending-dwell-ledger-strict", 6.8),
            CapPendingDwellLedgerAdjuster(41077.0),
            TieredPendingDwellLedgerAdjuster(
                mutableListOf<Double>(5886.0, 5909.0),
                mutableListOf<Double>(1.16, 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 66279.6,
    )
}
