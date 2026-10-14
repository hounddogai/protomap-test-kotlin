package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional lease return baseline calculation. */
interface RegionalLeaseReturnBaselineStep {
    fun describe(): String
    fun adjust(item: RegionalLeaseReturnBaseline, value: Double): Double
}

data class RegionalLeaseReturnBaselineStartsAtOffsetAdjuster(
    var weight: Double,
) : RegionalLeaseReturnBaselineStep {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: RegionalLeaseReturnBaseline, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class MinimumRegionalLeaseReturnBaselineAdjuster(var minimum: Double) : RegionalLeaseReturnBaselineStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalLeaseReturnBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRegionalLeaseReturnBaselineAdjuster(var percent: Double) : RegionalLeaseReturnBaselineStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalLeaseReturnBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRegionalLeaseReturnBaselineAdjuster(var step: Double) : RegionalLeaseReturnBaselineStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalLeaseReturnBaseline, value: Double): Double = roundTo(value, step)
}

data class TieredRegionalLeaseReturnBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalLeaseReturnBaselineStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalLeaseReturnBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRegionalLeaseReturnBaselineAdjuster(var cap: Double) : RegionalLeaseReturnBaselineStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalLeaseReturnBaseline, value: Double): Double = minOf(value, cap)
}

/** Runs the regional lease return baseline steps in order and keeps a trace of each. */
data class RegionalLeaseReturnBaselineEngine(
    var steps: MutableList<RegionalLeaseReturnBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalLeaseReturnBaseline): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalLeaseReturnBaselineStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalLeaseReturnBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalLeaseReturnBaselineEngine(): RegionalLeaseReturnBaselineEngine {
    return RegionalLeaseReturnBaselineEngine(
        steps = mutableListOf<RegionalLeaseReturnBaselineStep>(
            RegionalLeaseReturnBaselineStartsAtOffsetAdjuster(1.28),
            MinimumRegionalLeaseReturnBaselineAdjuster(43.8),
            PercentRegionalLeaseReturnBaselineAdjuster(2.6),
            RoundingRegionalLeaseReturnBaselineAdjuster(1.0),
            TieredRegionalLeaseReturnBaselineAdjuster(
                mutableListOf<Double>(3116.0, 6597.0),
                mutableListOf<Double>(1.03, 1.09),
            ),
            CapRegionalLeaseReturnBaselineAdjuster(10969.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 89340.0,
    )
}
