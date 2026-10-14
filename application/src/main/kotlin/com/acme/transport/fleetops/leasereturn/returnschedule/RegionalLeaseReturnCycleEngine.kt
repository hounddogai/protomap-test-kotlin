package com.acme.transport.fleetops.leasereturn.returnschedule

/** One step of the regional lease return cycle calculation. */
interface RegionalLeaseReturnCycleFactor {
    fun describe(): String
    fun adjust(item: RegionalLeaseReturnCycle, value: Double): Double
}

data class TieredRegionalLeaseReturnCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalLeaseReturnCycleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalLeaseReturnCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRegionalLeaseReturnCycleAdjuster(var percent: Double) : RegionalLeaseReturnCycleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalLeaseReturnCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRegionalLeaseReturnCycleAdjuster(var minimum: Double) : RegionalLeaseReturnCycleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalLeaseReturnCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional lease return cycle steps in order and keeps a trace of each. */
data class RegionalLeaseReturnCycleEngine(
    var steps: MutableList<RegionalLeaseReturnCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalLeaseReturnCycle): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalLeaseReturnCycleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalLeaseReturnCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalLeaseReturnCycleEngine(): RegionalLeaseReturnCycleEngine {
    return RegionalLeaseReturnCycleEngine(
        steps = mutableListOf<RegionalLeaseReturnCycleFactor>(
            TieredRegionalLeaseReturnCycleAdjuster(
                mutableListOf<Double>(2857.0, 3715.0, 6070.0),
                mutableListOf<Double>(1.01, 1.05, 1.14),
            ),
            PercentRegionalLeaseReturnCycleAdjuster(-19.4),
            MinimumRegionalLeaseReturnCycleAdjuster(18.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 15926.3,
    )
}
