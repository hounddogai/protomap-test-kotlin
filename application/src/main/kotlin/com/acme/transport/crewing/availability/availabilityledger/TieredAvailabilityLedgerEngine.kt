package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered availability ledger calculation. */
interface TieredAvailabilityLedgerStep {
    fun describe(): String
    fun adjust(item: TieredAvailabilityLedger, value: Double): Double
}

data class FlaggedTieredAvailabilityLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredAvailabilityLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredAvailabilityLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTieredAvailabilityLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredAvailabilityLedgerStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredAvailabilityLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTieredAvailabilityLedgerAdjuster(var percent: Double) : TieredAvailabilityLedgerStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredAvailabilityLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SegmentTieredAvailabilityLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredAvailabilityLedgerStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: TieredAvailabilityLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTieredAvailabilityLedgerAdjuster(var cap: Double) : TieredAvailabilityLedgerStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredAvailabilityLedger, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered availability ledger steps in order and keeps a trace of each. */
data class TieredAvailabilityLedgerEngine(
    var steps: MutableList<TieredAvailabilityLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredAvailabilityLedger): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredAvailabilityLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredAvailabilityLedgerEngine(flags: Registry): TieredAvailabilityLedgerEngine {
    return TieredAvailabilityLedgerEngine(
        steps = mutableListOf<TieredAvailabilityLedgerStep>(
            FlaggedTieredAvailabilityLedgerAdjuster(flags, "tiered-availability-ledger-lenient", 7.3),
            TieredTieredAvailabilityLedgerAdjuster(
                mutableListOf<Double>(2555.0, 4567.0),
                mutableListOf<Double>(0.85, 1.15),
            ),
            PercentTieredAvailabilityLedgerAdjuster(-14.5),
            SegmentTieredAvailabilityLedgerAdjuster(
                mutableMapOf<String, Double>("central" to 0.95, "north" to 1.04, "south" to 0.87),
            ),
            CapTieredAvailabilityLedgerAdjuster(1047.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 12349.0,
    )
}
