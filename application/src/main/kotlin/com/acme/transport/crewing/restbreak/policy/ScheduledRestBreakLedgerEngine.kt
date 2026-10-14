package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled rest break ledger calculation. */
interface ScheduledRestBreakLedgerFactor {
    fun describe(): String
    fun adjust(item: ScheduledRestBreakLedger, value: Double): Double
}

data class MinimumScheduledRestBreakLedgerAdjuster(var minimum: Double) : ScheduledRestBreakLedgerFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledRestBreakLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ScheduledRestBreakLedgerRevisionOffsetAdjuster(var weight: Double) : ScheduledRestBreakLedgerFactor {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: ScheduledRestBreakLedger, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class TieredScheduledRestBreakLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledRestBreakLedgerFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledRestBreakLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled rest break ledger steps in order and keeps a trace of each. */
data class ScheduledRestBreakLedgerEngine(
    var steps: MutableList<ScheduledRestBreakLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledRestBreakLedger): Double {
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

    fun evaluateAll(items: MutableList<ScheduledRestBreakLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledRestBreakLedgerEngine(): ScheduledRestBreakLedgerEngine {
    return ScheduledRestBreakLedgerEngine(
        steps = mutableListOf<ScheduledRestBreakLedgerFactor>(
            MinimumScheduledRestBreakLedgerAdjuster(33.1),
            ScheduledRestBreakLedgerRevisionOffsetAdjuster(0.19),
            TieredScheduledRestBreakLedgerAdjuster(
                mutableListOf<Double>(2207.0, 7263.0, 7825.0, 9634.0),
                mutableListOf<Double>(0.85, 1.0, 1.1, 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 40360.9,
    )
}
