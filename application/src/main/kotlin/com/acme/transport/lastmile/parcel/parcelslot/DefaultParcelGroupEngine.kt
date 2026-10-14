package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.kit.numbers.roundTo

/** One step of the default parcel group calculation. */
interface DefaultParcelGroupAdjuster {
    fun describe(): String
    fun adjust(item: DefaultParcelGroup, value: Double): Double
}

data class RegionDefaultParcelGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultParcelGroupAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: DefaultParcelGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDefaultParcelGroupAdjuster(var step: Double) : DefaultParcelGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultParcelGroup, value: Double): Double = roundTo(value, step)
}

data class PercentDefaultParcelGroupAdjuster(var percent: Double) : DefaultParcelGroupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultParcelGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDefaultParcelGroupAdjuster(var minimum: Double) : DefaultParcelGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultParcelGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDefaultParcelGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultParcelGroupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultParcelGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default parcel group steps in order and keeps a trace of each. */
data class DefaultParcelGroupEngine(
    var steps: MutableList<DefaultParcelGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultParcelGroup): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultParcelGroupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultParcelGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultParcelGroupEngine(): DefaultParcelGroupEngine {
    return DefaultParcelGroupEngine(
        steps = mutableListOf<DefaultParcelGroupAdjuster>(
            RegionDefaultParcelGroupAdjuster(
                mutableMapOf<String, Double>("east" to 1.03, "retail" to 1.17, "central" to 1.1),
            ),
            RoundingDefaultParcelGroupAdjuster(0.1),
            PercentDefaultParcelGroupAdjuster(-23.9),
            MinimumDefaultParcelGroupAdjuster(18.6),
            TieredDefaultParcelGroupAdjuster(
                mutableListOf<Double>(2161.0, 5026.0, 6707.0),
                mutableListOf<Double>(1.09, 1.03, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 1698.8,
    )
}
