package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the near miss ledger calculation. */
interface NearMissLedgerFactor {
    fun describe(): String
    fun adjust(item: NearMissLedger, value: Double): Double
}

data class NameNearMissLedgerAdjuster(var factors: MutableMap<String, Double>) : NearMissLedgerFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: NearMissLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapNearMissLedgerAdjuster(var cap: Double) : NearMissLedgerFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissLedger, value: Double): Double = minOf(value, cap)
}

data class FlaggedNearMissLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NearMissLedgerFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NearMissLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingNearMissLedgerAdjuster(var step: Double) : NearMissLedgerFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NearMissLedger, value: Double): Double = roundTo(value, step)
}

data class NearMissLedgerExpiresAtOffsetAdjuster(var weight: Double) : NearMissLedgerFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: NearMissLedger, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class TieredNearMissLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NearMissLedgerFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NearMissLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the near miss ledger steps in order and keeps a trace of each. */
data class NearMissLedgerEngine(
    var steps: MutableList<NearMissLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: NearMissLedger): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NearMissLedgerFactor = steps[index]
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

    fun evaluateAll(items: MutableList<NearMissLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultNearMissLedgerEngine(flags: Registry): NearMissLedgerEngine {
    return NearMissLedgerEngine(
        steps = mutableListOf<NearMissLedgerFactor>(
            NameNearMissLedgerAdjuster(mutableMapOf<String, Double>("central" to 1.22)),
            CapNearMissLedgerAdjuster(43113.0),
            FlaggedNearMissLedgerAdjuster(flags, "near-miss-ledger-capped", 7.9),
            RoundingNearMissLedgerAdjuster(5.0),
            NearMissLedgerExpiresAtOffsetAdjuster(0.16),
            TieredNearMissLedgerAdjuster(
                mutableListOf<Double>(5754.0, 8588.0, 9791.0),
                mutableListOf<Double>(0.96, 0.88, 1.05),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 42418.0,
    )
}
