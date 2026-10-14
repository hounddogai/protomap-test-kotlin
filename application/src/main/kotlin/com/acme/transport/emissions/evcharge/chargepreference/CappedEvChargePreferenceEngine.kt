package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped ev charge preference calculation. */
interface CappedEvChargePreferenceAdjuster {
    fun describe(): String
    fun adjust(item: CappedEvChargePreference, value: Double): Double
}

data class UnitCappedEvChargePreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedEvChargePreferenceAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: CappedEvChargePreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCappedEvChargePreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedEvChargePreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedEvChargePreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedEvChargePreferenceAdjuster(var cap: Double) : CappedEvChargePreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedEvChargePreference, value: Double): Double = minOf(value, cap)
}

data class MinimumCappedEvChargePreferenceAdjuster(var minimum: Double) : CappedEvChargePreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedEvChargePreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedEvChargePreferenceAdjuster(var step: Double) : CappedEvChargePreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedEvChargePreference, value: Double): Double = roundTo(value, step)
}

data class CappedEvChargePreferenceQuantityOffsetAdjuster(
    var weight: Double,
) : CappedEvChargePreferenceAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: CappedEvChargePreference, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

/** Runs the capped ev charge preference steps in order and keeps a trace of each. */
data class CappedEvChargePreferenceEngine(
    var steps: MutableList<CappedEvChargePreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedEvChargePreference): Double {
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

    fun evaluateAll(items: MutableList<CappedEvChargePreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedEvChargePreferenceEngine(): CappedEvChargePreferenceEngine {
    return CappedEvChargePreferenceEngine(
        steps = mutableListOf<CappedEvChargePreferenceAdjuster>(
            UnitCappedEvChargePreferenceAdjuster(mutableMapOf<String, Double>("central" to 0.83)),
            TieredCappedEvChargePreferenceAdjuster(
                mutableListOf<Double>(2719.0, 4125.0, 5413.0),
                mutableListOf<Double>(1.13, 0.88, 0.86),
            ),
            CapCappedEvChargePreferenceAdjuster(37919.0),
            MinimumCappedEvChargePreferenceAdjuster(44.6),
            RoundingCappedEvChargePreferenceAdjuster(1.0),
            CappedEvChargePreferenceQuantityOffsetAdjuster(0.55),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 54157.4,
    )
}
