package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted accessorial profile calculation. */
interface WeightedAccessorialProfileStep {
    fun describe(): String
    fun adjust(item: WeightedAccessorialProfile, value: Double): Double
}

data class CapWeightedAccessorialProfileAdjuster(var cap: Double) : WeightedAccessorialProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedAccessorialProfile, value: Double): Double = minOf(value, cap)
}

data class WeightedAccessorialProfileRetriesOffsetAdjuster(
    var weight: Double,
) : WeightedAccessorialProfileStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: WeightedAccessorialProfile, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class TieredWeightedAccessorialProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedAccessorialProfileStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedAccessorialProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentWeightedAccessorialProfileAdjuster(var percent: Double) : WeightedAccessorialProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedAccessorialProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted accessorial profile steps in order and keeps a trace of each. */
data class WeightedAccessorialProfileEngine(
    var steps: MutableList<WeightedAccessorialProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedAccessorialProfile): Double {
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

    fun evaluateAll(items: MutableList<WeightedAccessorialProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedAccessorialProfileEngine(): WeightedAccessorialProfileEngine {
    return WeightedAccessorialProfileEngine(
        steps = mutableListOf<WeightedAccessorialProfileStep>(
            CapWeightedAccessorialProfileAdjuster(23069.0),
            WeightedAccessorialProfileRetriesOffsetAdjuster(0.52),
            TieredWeightedAccessorialProfileAdjuster(
                mutableListOf<Double>(2678.0, 3338.0, 4301.0, 9170.0),
                mutableListOf<Double>(1.15, 1.2, 0.81, 0.98),
            ),
            PercentWeightedAccessorialProfileAdjuster(-2.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 3696.9,
    )
}
