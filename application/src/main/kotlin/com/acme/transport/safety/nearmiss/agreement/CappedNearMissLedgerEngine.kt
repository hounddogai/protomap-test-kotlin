package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped near miss ledger calculation. */
interface CappedNearMissLedgerAdjuster {
    fun describe(): String
    fun adjust(item: CappedNearMissLedger, value: Double): Double
}

data class PercentCappedNearMissLedgerAdjuster(var percent: Double) : CappedNearMissLedgerAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedNearMissLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCappedNearMissLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedNearMissLedgerAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedNearMissLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCappedNearMissLedgerAdjuster(var step: Double) : CappedNearMissLedgerAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedNearMissLedger, value: Double): Double = roundTo(value, step)
}

/** Runs the capped near miss ledger steps in order and keeps a trace of each. */
data class CappedNearMissLedgerEngine(
    var steps: MutableList<CappedNearMissLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedNearMissLedger): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedNearMissLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedNearMissLedgerEngine(flags: Registry): CappedNearMissLedgerEngine {
    return CappedNearMissLedgerEngine(
        steps = mutableListOf<CappedNearMissLedgerAdjuster>(
            PercentCappedNearMissLedgerAdjuster(-19.1),
            FlaggedCappedNearMissLedgerAdjuster(flags, "capped-near-miss-ledger-batched", -9.2),
            RoundingCappedNearMissLedgerAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 87250.5,
    )
}
