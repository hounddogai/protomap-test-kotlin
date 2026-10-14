package com.acme.transport.network.zone.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual zone ledger calculation. */
interface ManualZoneLedgerAdjuster {
    fun describe(): String
    fun adjust(item: ManualZoneLedger, value: Double): Double
}

data class RoundingManualZoneLedgerAdjuster(var step: Double) : ManualZoneLedgerAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualZoneLedger, value: Double): Double = roundTo(value, step)
}

data class TieredManualZoneLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualZoneLedgerAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualZoneLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedManualZoneLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualZoneLedgerAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualZoneLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumManualZoneLedgerAdjuster(var minimum: Double) : ManualZoneLedgerAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualZoneLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentManualZoneLedgerAdjuster(var percent: Double) : ManualZoneLedgerAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualZoneLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the manual zone ledger steps in order and keeps a trace of each. */
data class ManualZoneLedgerEngine(
    var steps: MutableList<ManualZoneLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualZoneLedger): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualZoneLedgerAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualZoneLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualZoneLedgerEngine(flags: Registry): ManualZoneLedgerEngine {
    return ManualZoneLedgerEngine(
        steps = mutableListOf<ManualZoneLedgerAdjuster>(
            RoundingManualZoneLedgerAdjuster(0.25),
            TieredManualZoneLedgerAdjuster(
                mutableListOf<Double>(1128.0, 7444.0),
                mutableListOf<Double>(1.16, 1.07),
            ),
            FlaggedManualZoneLedgerAdjuster(flags, "manual-zone-ledger-scheduled", -0.5),
            MinimumManualZoneLedgerAdjuster(10.6),
            PercentManualZoneLedgerAdjuster(13.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 22113.6,
    )
}
