package com.acme.transport.customs.hold.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the hold ledger calculation. */
interface HoldLedgerStep {
    fun describe(): String
    fun adjust(item: HoldLedger, value: Double): Double
}

data class MinimumHoldLedgerAdjuster(var minimum: Double) : HoldLedgerStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HoldLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedHoldLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapHoldLedgerAdjuster(var cap: Double) : HoldLedgerStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HoldLedger, value: Double): Double = minOf(value, cap)
}

/** Runs the hold ledger steps in order and keeps a trace of each. */
data class HoldLedgerEngine(
    var steps: MutableList<HoldLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HoldLedger): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HoldLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHoldLedgerEngine(flags: Registry): HoldLedgerEngine {
    return HoldLedgerEngine(
        steps = mutableListOf<HoldLedgerStep>(
            MinimumHoldLedgerAdjuster(37.4),
            FlaggedHoldLedgerAdjuster(flags, "hold-ledger-capped", 6.6),
            CapHoldLedgerAdjuster(32432.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 85585.4,
    )
}
