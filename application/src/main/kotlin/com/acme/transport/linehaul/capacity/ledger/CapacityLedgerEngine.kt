package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capacity ledger calculation. */
interface CapacityLedgerAdjuster {
    fun describe(): String
    fun adjust(item: CapacityLedger, value: Double): Double
}

data class FlaggedCapacityLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityLedgerAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelCapacityLedgerAdjuster(var factors: MutableMap<String, Double>) : CapacityLedgerAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: CapacityLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCapacityLedgerAdjuster(var minimum: Double) : CapacityLedgerAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCapacityLedgerAdjuster(var percent: Double) : CapacityLedgerAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CapacityLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCapacityLedgerAdjuster(var step: Double) : CapacityLedgerAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacityLedger, value: Double): Double = roundTo(value, step)
}

data class CapacityLedgerRevisionOffsetAdjuster(var weight: Double) : CapacityLedgerAdjuster {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: CapacityLedger, value: Double): Double = value + item.revision.toDouble() * weight
}

/** Runs the capacity ledger steps in order and keeps a trace of each. */
data class CapacityLedgerEngine(
    var steps: MutableList<CapacityLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CapacityLedger): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CapacityLedgerAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CapacityLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCapacityLedgerEngine(flags: Registry): CapacityLedgerEngine {
    return CapacityLedgerEngine(
        steps = mutableListOf<CapacityLedgerAdjuster>(
            FlaggedCapacityLedgerAdjuster(flags, "capacity-ledger-batched", 9.5),
            LabelCapacityLedgerAdjuster(mutableMapOf<String, Double>("east" to 1.11)),
            MinimumCapacityLedgerAdjuster(16.5),
            PercentCapacityLedgerAdjuster(17.6),
            RoundingCapacityLedgerAdjuster(0.25),
            CapacityLedgerRevisionOffsetAdjuster(1.74),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 61865.2,
    )
}
