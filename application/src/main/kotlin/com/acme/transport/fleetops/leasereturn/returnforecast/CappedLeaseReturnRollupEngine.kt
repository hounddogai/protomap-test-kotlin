package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped lease return rollup calculation. */
interface CappedLeaseReturnRollupFactor {
    fun describe(): String
    fun adjust(item: CappedLeaseReturnRollup, value: Double): Double
}

data class RoundingCappedLeaseReturnRollupAdjuster(var step: Double) : CappedLeaseReturnRollupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedLeaseReturnRollup, value: Double): Double = roundTo(value, step)
}

data class MinimumCappedLeaseReturnRollupAdjuster(var minimum: Double) : CappedLeaseReturnRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedLeaseReturnRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCappedLeaseReturnRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedLeaseReturnRollupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedLeaseReturnRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped lease return rollup steps in order and keeps a trace of each. */
data class CappedLeaseReturnRollupEngine(
    var steps: MutableList<CappedLeaseReturnRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedLeaseReturnRollup): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedLeaseReturnRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedLeaseReturnRollupEngine(): CappedLeaseReturnRollupEngine {
    return CappedLeaseReturnRollupEngine(
        steps = mutableListOf<CappedLeaseReturnRollupFactor>(
            RoundingCappedLeaseReturnRollupAdjuster(1.0),
            MinimumCappedLeaseReturnRollupAdjuster(20.9),
            TieredCappedLeaseReturnRollupAdjuster(
                mutableListOf<Double>(4402.0, 8005.0),
                mutableListOf<Double>(1.03, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 21064.6,
    )
}
