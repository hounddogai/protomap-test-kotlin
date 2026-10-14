package com.acme.transport.crewing.availability.ledger

import com.acme.transport.kit.flags.Registry

/** One step of the availability ledger calculation. */
interface AvailabilityLedgerModifier {
    fun describe(): String
    fun adjust(item: AvailabilityLedger, value: Double): Double
}

data class MinimumAvailabilityLedgerAdjuster(var minimum: Double) : AvailabilityLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AvailabilityLedgerCostOffsetAdjuster(var weight: Double) : AvailabilityLedgerModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: AvailabilityLedger, value: Double): Double = value + item.cost * weight
}

data class FlaggedAvailabilityLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilityLedgerModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilityLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the availability ledger steps in order and keeps a trace of each. */
data class AvailabilityLedgerEngine(
    var steps: MutableList<AvailabilityLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AvailabilityLedger): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AvailabilityLedgerModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AvailabilityLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAvailabilityLedgerEngine(flags: Registry): AvailabilityLedgerEngine {
    return AvailabilityLedgerEngine(
        steps = mutableListOf<AvailabilityLedgerModifier>(
            MinimumAvailabilityLedgerAdjuster(9.0),
            AvailabilityLedgerCostOffsetAdjuster(0.83),
            FlaggedAvailabilityLedgerAdjuster(flags, "availability-ledger-fallback", 3.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 48300.3,
    )
}
