package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rest break ledger calculation. */
interface RestBreakLedgerAdjuster {
    fun describe(): String
    fun adjust(item: RestBreakLedger, value: Double): Double
}

data class FlaggedRestBreakLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakLedgerAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionRestBreakLedgerAdjuster(var factors: MutableMap<String, Double>) : RestBreakLedgerAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: RestBreakLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRestBreakLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakLedgerAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRestBreakLedgerAdjuster(var step: Double) : RestBreakLedgerAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakLedger, value: Double): Double = roundTo(value, step)
}

data class CapRestBreakLedgerAdjuster(var cap: Double) : RestBreakLedgerAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RestBreakLedger, value: Double): Double = minOf(value, cap)
}

data class RestBreakLedgerAmountOffsetAdjuster(var weight: Double) : RestBreakLedgerAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: RestBreakLedger, value: Double): Double = value + item.amount * weight
}

/** Runs the rest break ledger steps in order and keeps a trace of each. */
data class RestBreakLedgerEngine(
    var steps: MutableList<RestBreakLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RestBreakLedger): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RestBreakLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRestBreakLedgerEngine(flags: Registry): RestBreakLedgerEngine {
    return RestBreakLedgerEngine(
        steps = mutableListOf<RestBreakLedgerAdjuster>(
            FlaggedRestBreakLedgerAdjuster(flags, "rest-break-ledger-rolling", 5.3),
            RegionRestBreakLedgerAdjuster(mutableMapOf<String, Double>("online" to 0.94, "central" to 0.7)),
            TieredRestBreakLedgerAdjuster(
                mutableListOf<Double>(2411.0, 4511.0, 5978.0, 9681.0),
                mutableListOf<Double>(0.95, 0.93, 1.04, 0.87),
            ),
            RoundingRestBreakLedgerAdjuster(0.05),
            CapRestBreakLedgerAdjuster(14485.0),
            RestBreakLedgerAmountOffsetAdjuster(1.84),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 43362.3,
    )
}
