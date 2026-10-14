package com.acme.transport.terminal.sortation.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled sortation ledger calculation. */
interface ScheduledSortationLedgerModifier {
    fun describe(): String
    fun adjust(item: ScheduledSortationLedger, value: Double): Double
}

data class RoundingScheduledSortationLedgerAdjuster(var step: Double) : ScheduledSortationLedgerModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledSortationLedger, value: Double): Double = roundTo(value, step)
}

data class CapScheduledSortationLedgerAdjuster(var cap: Double) : ScheduledSortationLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledSortationLedger, value: Double): Double = minOf(value, cap)
}

data class FlaggedScheduledSortationLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledSortationLedgerModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledSortationLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScheduledSortationLedgerAdjuster(var minimum: Double) : ScheduledSortationLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledSortationLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ScheduledSortationLedgerAmountOffsetAdjuster(var weight: Double) : ScheduledSortationLedgerModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: ScheduledSortationLedger, value: Double): Double = value + item.amount * weight
}

data class TieredScheduledSortationLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledSortationLedgerModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledSortationLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled sortation ledger steps in order and keeps a trace of each. */
data class ScheduledSortationLedgerEngine(
    var steps: MutableList<ScheduledSortationLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledSortationLedger): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledSortationLedgerModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledSortationLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledSortationLedgerEngine(flags: Registry): ScheduledSortationLedgerEngine {
    return ScheduledSortationLedgerEngine(
        steps = mutableListOf<ScheduledSortationLedgerModifier>(
            RoundingScheduledSortationLedgerAdjuster(0.1),
            CapScheduledSortationLedgerAdjuster(16385.0),
            FlaggedScheduledSortationLedgerAdjuster(flags, "scheduled-sortation-ledger-seasonal", 1.5),
            MinimumScheduledSortationLedgerAdjuster(28.5),
            ScheduledSortationLedgerAmountOffsetAdjuster(0.61),
            TieredScheduledSortationLedgerAdjuster(
                mutableListOf<Double>(2741.0, 6516.0, 6975.0),
                mutableListOf<Double>(1.16, 1.16, 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 81077.0,
    )
}
