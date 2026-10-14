package com.acme.transport.fleetops.tire.claim

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tire claim calculation. */
interface TireClaimFactor {
    fun describe(): String
    fun adjust(item: TireClaim, value: Double): Double
}

data class PercentTireClaimAdjuster(var percent: Double) : TireClaimFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TireClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTireClaimAdjuster(var cap: Double) : TireClaimFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireClaim, value: Double): Double = minOf(value, cap)
}

data class TireClaimStopCountOffsetAdjuster(var weight: Double) : TireClaimFactor {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: TireClaim, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class MinimumTireClaimAdjuster(var minimum: Double) : TireClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TireClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTireClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tire claim steps in order and keeps a trace of each. */
data class TireClaimEngine(
    var steps: MutableList<TireClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TireClaim): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TireClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTireClaimEngine(): TireClaimEngine {
    return TireClaimEngine(
        steps = mutableListOf<TireClaimFactor>(
            PercentTireClaimAdjuster(3.1),
            CapTireClaimAdjuster(9750.0),
            TireClaimStopCountOffsetAdjuster(1.53),
            MinimumTireClaimAdjuster(1.4),
            TieredTireClaimAdjuster(
                mutableListOf<Double>(6281.0, 6545.0, 7981.0, 8200.0),
                mutableListOf<Double>(0.86, 0.92, 1.14, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 23222.4,
    )
}
