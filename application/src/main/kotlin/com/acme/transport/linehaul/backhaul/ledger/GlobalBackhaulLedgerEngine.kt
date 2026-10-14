package com.acme.transport.linehaul.backhaul.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global backhaul ledger calculation. */
interface GlobalBackhaulLedgerStep {
    fun describe(): String
    fun adjust(item: GlobalBackhaulLedger, value: Double): Double
}

data class RoundingGlobalBackhaulLedgerAdjuster(var step: Double) : GlobalBackhaulLedgerStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalBackhaulLedger, value: Double): Double = roundTo(value, step)
}

data class FlaggedGlobalBackhaulLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalBackhaulLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalBackhaulLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumGlobalBackhaulLedgerAdjuster(var minimum: Double) : GlobalBackhaulLedgerStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalBackhaulLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapGlobalBackhaulLedgerAdjuster(var cap: Double) : GlobalBackhaulLedgerStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalBackhaulLedger, value: Double): Double = minOf(value, cap)
}

/** Runs the global backhaul ledger steps in order and keeps a trace of each. */
data class GlobalBackhaulLedgerEngine(
    var steps: MutableList<GlobalBackhaulLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalBackhaulLedger): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalBackhaulLedgerStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalBackhaulLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalBackhaulLedgerEngine(flags: Registry): GlobalBackhaulLedgerEngine {
    return GlobalBackhaulLedgerEngine(
        steps = mutableListOf<GlobalBackhaulLedgerStep>(
            RoundingGlobalBackhaulLedgerAdjuster(0.1),
            FlaggedGlobalBackhaulLedgerAdjuster(flags, "global-backhaul-ledger-adaptive", -7.4),
            MinimumGlobalBackhaulLedgerAdjuster(20.8),
            CapGlobalBackhaulLedgerAdjuster(753.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 53202.6,
    )
}
