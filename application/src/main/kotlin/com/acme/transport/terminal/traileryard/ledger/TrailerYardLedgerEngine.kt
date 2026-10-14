package com.acme.transport.terminal.traileryard.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the trailer yard ledger calculation. */
interface TrailerYardLedgerFactor {
    fun describe(): String
    fun adjust(item: TrailerYardLedger, value: Double): Double
}

data class TrailerYardLedgerEndsAtOffsetAdjuster(var weight: Double) : TrailerYardLedgerFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: TrailerYardLedger, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class SegmentTrailerYardLedgerAdjuster(var factors: MutableMap<String, Double>) : TrailerYardLedgerFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: TrailerYardLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTrailerYardLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerYardLedgerFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerYardLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTrailerYardLedgerAdjuster(var minimum: Double) : TrailerYardLedgerFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerYardLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingTrailerYardLedgerAdjuster(var step: Double) : TrailerYardLedgerFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerYardLedger, value: Double): Double = roundTo(value, step)
}

/** Runs the trailer yard ledger steps in order and keeps a trace of each. */
data class TrailerYardLedgerEngine(
    var steps: MutableList<TrailerYardLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TrailerYardLedger): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerYardLedgerFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerYardLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTrailerYardLedgerEngine(flags: Registry): TrailerYardLedgerEngine {
    return TrailerYardLedgerEngine(
        steps = mutableListOf<TrailerYardLedgerFactor>(
            TrailerYardLedgerEndsAtOffsetAdjuster(0.59),
            SegmentTrailerYardLedgerAdjuster(mutableMapOf<String, Double>("east" to 1.4)),
            FlaggedTrailerYardLedgerAdjuster(flags, "trailer-yard-ledger-seasonal", 2.9),
            MinimumTrailerYardLedgerAdjuster(14.7),
            RoundingTrailerYardLedgerAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 45565.7,
    )
}
