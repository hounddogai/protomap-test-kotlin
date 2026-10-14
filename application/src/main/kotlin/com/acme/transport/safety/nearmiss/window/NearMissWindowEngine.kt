package com.acme.transport.safety.nearmiss.window

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the near miss window calculation. */
interface NearMissWindowAdjuster {
    fun describe(): String
    fun adjust(item: NearMissWindow, value: Double): Double
}

data class TieredNearMissWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NearMissWindowAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NearMissWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumNearMissWindowAdjuster(var minimum: Double) : NearMissWindowAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapNearMissWindowAdjuster(var cap: Double) : NearMissWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissWindow, value: Double): Double = minOf(value, cap)
}

data class PercentNearMissWindowAdjuster(var percent: Double) : NearMissWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NearMissWindowRateOffsetAdjuster(var weight: Double) : NearMissWindowAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: NearMissWindow, value: Double): Double = value + item.rate * weight
}

/** Runs the near miss window steps in order and keeps a trace of each. */
data class NearMissWindowEngine(
    var steps: MutableList<NearMissWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: NearMissWindow): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NearMissWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultNearMissWindowEngine(): NearMissWindowEngine {
    return NearMissWindowEngine(
        steps = mutableListOf<NearMissWindowAdjuster>(
            TieredNearMissWindowAdjuster(
                mutableListOf<Double>(4337.0, 6673.0),
                mutableListOf<Double>(0.97, 1.04),
            ),
            MinimumNearMissWindowAdjuster(12.2),
            CapNearMissWindowAdjuster(33328.0),
            PercentNearMissWindowAdjuster(-18.1),
            NearMissWindowRateOffsetAdjuster(1.22),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 45542.2,
    )
}
