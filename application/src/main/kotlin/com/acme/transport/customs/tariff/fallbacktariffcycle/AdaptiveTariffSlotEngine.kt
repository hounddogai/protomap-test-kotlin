package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive tariff slot calculation. */
interface AdaptiveTariffSlotFactor {
    fun describe(): String
    fun adjust(item: AdaptiveTariffSlot, value: Double): Double
}

data class FlaggedAdaptiveTariffSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveTariffSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveTariffSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAdaptiveTariffSlotAdjuster(var cap: Double) : AdaptiveTariffSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveTariffSlot, value: Double): Double = minOf(value, cap)
}

data class TitleAdaptiveTariffSlotAdjuster(var factors: MutableMap<String, Double>) : AdaptiveTariffSlotFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveTariffSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAdaptiveTariffSlotAdjuster(var percent: Double) : AdaptiveTariffSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveTariffSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AdaptiveTariffSlotCapacityOffsetAdjuster(var weight: Double) : AdaptiveTariffSlotFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: AdaptiveTariffSlot, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class TieredAdaptiveTariffSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveTariffSlotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveTariffSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive tariff slot steps in order and keeps a trace of each. */
data class AdaptiveTariffSlotEngine(
    var steps: MutableList<AdaptiveTariffSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveTariffSlot): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveTariffSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveTariffSlotEngine(flags: Registry): AdaptiveTariffSlotEngine {
    return AdaptiveTariffSlotEngine(
        steps = mutableListOf<AdaptiveTariffSlotFactor>(
            FlaggedAdaptiveTariffSlotAdjuster(flags, "adaptive-tariff-slot-strict", 3.7),
            CapAdaptiveTariffSlotAdjuster(34912.0),
            TitleAdaptiveTariffSlotAdjuster(
                mutableMapOf<String, Double>("online" to 0.76, "central" to 1.38, "north" to 1.29),
            ),
            PercentAdaptiveTariffSlotAdjuster(10.1),
            AdaptiveTariffSlotCapacityOffsetAdjuster(1.21),
            TieredAdaptiveTariffSlotAdjuster(
                mutableListOf<Double>(3260.0, 3476.0),
                mutableListOf<Double>(1.1, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 21160.7,
    )
}
