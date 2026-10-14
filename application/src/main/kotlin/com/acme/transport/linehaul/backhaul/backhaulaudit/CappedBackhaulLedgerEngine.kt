package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped backhaul ledger calculation. */
interface CappedBackhaulLedgerStep {
    fun describe(): String
    fun adjust(item: CappedBackhaulLedger, value: Double): Double
}

data class MinimumCappedBackhaulLedgerAdjuster(var minimum: Double) : CappedBackhaulLedgerStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedBackhaulLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CappedBackhaulLedgerLoadKgOffsetAdjuster(var weight: Double) : CappedBackhaulLedgerStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: CappedBackhaulLedger, value: Double): Double = value + item.loadKg * weight
}

data class TieredCappedBackhaulLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedBackhaulLedgerStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedBackhaulLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCappedBackhaulLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedBackhaulLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedBackhaulLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCappedBackhaulLedgerAdjuster(var step: Double) : CappedBackhaulLedgerStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedBackhaulLedger, value: Double): Double = roundTo(value, step)
}

data class LocaleCappedBackhaulLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedBackhaulLedgerStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: CappedBackhaulLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped backhaul ledger steps in order and keeps a trace of each. */
data class CappedBackhaulLedgerEngine(
    var steps: MutableList<CappedBackhaulLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedBackhaulLedger): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedBackhaulLedgerStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedBackhaulLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedBackhaulLedgerEngine(flags: Registry): CappedBackhaulLedgerEngine {
    return CappedBackhaulLedgerEngine(
        steps = mutableListOf<CappedBackhaulLedgerStep>(
            MinimumCappedBackhaulLedgerAdjuster(49.1),
            CappedBackhaulLedgerLoadKgOffsetAdjuster(0.72),
            TieredCappedBackhaulLedgerAdjuster(
                mutableListOf<Double>(965.0, 5029.0, 6942.0),
                mutableListOf<Double>(1.16, 1.08, 0.94),
            ),
            FlaggedCappedBackhaulLedgerAdjuster(flags, "capped-backhaul-ledger-lenient", 4.5),
            RoundingCappedBackhaulLedgerAdjuster(0.1),
            LocaleCappedBackhaulLedgerAdjuster(mutableMapOf<String, Double>("north" to 1.14, "west" to 0.98)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 30784.9,
    )
}
