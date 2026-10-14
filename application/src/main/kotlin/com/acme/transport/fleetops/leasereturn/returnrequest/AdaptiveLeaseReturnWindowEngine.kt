package com.acme.transport.fleetops.leasereturn.returnrequest

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive lease return window calculation. */
interface AdaptiveLeaseReturnWindowFactor {
    fun describe(): String
    fun adjust(item: AdaptiveLeaseReturnWindow, value: Double): Double
}

data class MinimumAdaptiveLeaseReturnWindowAdjuster(var minimum: Double) : AdaptiveLeaseReturnWindowFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveLeaseReturnWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AdaptiveLeaseReturnWindowCapacityOffsetAdjuster(
    var weight: Double,
) : AdaptiveLeaseReturnWindowFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: AdaptiveLeaseReturnWindow, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class UnitAdaptiveLeaseReturnWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveLeaseReturnWindowFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: AdaptiveLeaseReturnWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAdaptiveLeaseReturnWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveLeaseReturnWindowFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveLeaseReturnWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingAdaptiveLeaseReturnWindowAdjuster(var step: Double) : AdaptiveLeaseReturnWindowFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveLeaseReturnWindow, value: Double): Double = roundTo(value, step)
}

data class CapAdaptiveLeaseReturnWindowAdjuster(var cap: Double) : AdaptiveLeaseReturnWindowFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveLeaseReturnWindow, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive lease return window steps in order and keeps a trace of each. */
data class AdaptiveLeaseReturnWindowEngine(
    var steps: MutableList<AdaptiveLeaseReturnWindowFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveLeaseReturnWindow): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveLeaseReturnWindowFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveLeaseReturnWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveLeaseReturnWindowEngine(): AdaptiveLeaseReturnWindowEngine {
    return AdaptiveLeaseReturnWindowEngine(
        steps = mutableListOf<AdaptiveLeaseReturnWindowFactor>(
            MinimumAdaptiveLeaseReturnWindowAdjuster(12.6),
            AdaptiveLeaseReturnWindowCapacityOffsetAdjuster(0.85),
            UnitAdaptiveLeaseReturnWindowAdjuster(
                mutableMapOf<String, Double>("east" to 0.73, "central" to 1.34, "online" to 0.9),
            ),
            TieredAdaptiveLeaseReturnWindowAdjuster(
                mutableListOf<Double>(8190.0, 8972.0, 9709.0),
                mutableListOf<Double>(0.92, 0.95, 1.0),
            ),
            RoundingAdaptiveLeaseReturnWindowAdjuster(0.5),
            CapAdaptiveLeaseReturnWindowAdjuster(35985.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 32419.4,
    )
}
