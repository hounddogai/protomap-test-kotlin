package com.acme.transport.customs.broker.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global broker adjustment calculation. */
interface GlobalBrokerAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: GlobalBrokerAdjustment, value: Double): Double
}

data class MinimumGlobalBrokerAdjustmentAdjuster(var minimum: Double) : GlobalBrokerAdjustmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalBrokerAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class GlobalBrokerAdjustmentDistanceKmOffsetAdjuster(var weight: Double) : GlobalBrokerAdjustmentAdjuster {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: GlobalBrokerAdjustment, value: Double): Double = value + item.distanceKm * weight
}

data class FlaggedGlobalBrokerAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalBrokerAdjustmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalBrokerAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapGlobalBrokerAdjustmentAdjuster(var cap: Double) : GlobalBrokerAdjustmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalBrokerAdjustment, value: Double): Double = minOf(value, cap)
}

data class TieredGlobalBrokerAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalBrokerAdjustmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalBrokerAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global broker adjustment steps in order and keeps a trace of each. */
data class GlobalBrokerAdjustmentEngine(
    var steps: MutableList<GlobalBrokerAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalBrokerAdjustment): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalBrokerAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalBrokerAdjustmentEngine(flags: Registry): GlobalBrokerAdjustmentEngine {
    return GlobalBrokerAdjustmentEngine(
        steps = mutableListOf<GlobalBrokerAdjustmentAdjuster>(
            MinimumGlobalBrokerAdjustmentAdjuster(29.9),
            GlobalBrokerAdjustmentDistanceKmOffsetAdjuster(1.06),
            FlaggedGlobalBrokerAdjustmentAdjuster(flags, "global-broker-adjustment-manual", -3.0),
            CapGlobalBrokerAdjustmentAdjuster(46127.0),
            TieredGlobalBrokerAdjustmentAdjuster(
                mutableListOf<Double>(2091.0, 4316.0, 4407.0),
                mutableListOf<Double>(1.02, 1.05, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 68490.0,
    )
}
