package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the ev charge group calculation. */
interface EvChargeGroupModifier {
    fun describe(): String
    fun adjust(item: EvChargeGroup, value: Double): Double
}

data class EvChargeGroupQuantityOffsetAdjuster(var weight: Double) : EvChargeGroupModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: EvChargeGroup, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class CountryEvChargeGroupAdjuster(var factors: MutableMap<String, Double>) : EvChargeGroupModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: EvChargeGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedEvChargeGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeGroupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingEvChargeGroupAdjuster(var step: Double) : EvChargeGroupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeGroup, value: Double): Double = roundTo(value, step)
}

data class TieredEvChargeGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeGroupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumEvChargeGroupAdjuster(var minimum: Double) : EvChargeGroupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the ev charge group steps in order and keeps a trace of each. */
data class EvChargeGroupEngine(
    var steps: MutableList<EvChargeGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EvChargeGroup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EvChargeGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEvChargeGroupEngine(flags: Registry): EvChargeGroupEngine {
    return EvChargeGroupEngine(
        steps = mutableListOf<EvChargeGroupModifier>(
            EvChargeGroupQuantityOffsetAdjuster(1.58),
            CountryEvChargeGroupAdjuster(mutableMapOf<String, Double>("east" to 1.19)),
            FlaggedEvChargeGroupAdjuster(flags, "ev-charge-group-pending", 3.9),
            RoundingEvChargeGroupAdjuster(0.5),
            TieredEvChargeGroupAdjuster(
                mutableListOf<Double>(1285.0, 6248.0, 6868.0, 8217.0),
                mutableListOf<Double>(0.99, 1.07, 0.91, 0.93),
            ),
            MinimumEvChargeGroupAdjuster(10.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 66194.3,
    )
}
