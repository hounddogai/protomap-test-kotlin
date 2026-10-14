package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual rate card preference calculation. */
interface ManualRateCardPreferenceFactor {
    fun describe(): String
    fun adjust(item: ManualRateCardPreference, value: Double): Double
}

data class DepotManualRateCardPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualRateCardPreferenceFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: ManualRateCardPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapManualRateCardPreferenceAdjuster(var cap: Double) : ManualRateCardPreferenceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualRateCardPreference, value: Double): Double = minOf(value, cap)
}

data class TieredManualRateCardPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualRateCardPreferenceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualRateCardPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual rate card preference steps in order and keeps a trace of each. */
data class ManualRateCardPreferenceEngine(
    var steps: MutableList<ManualRateCardPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualRateCardPreference): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualRateCardPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualRateCardPreferenceEngine(): ManualRateCardPreferenceEngine {
    return ManualRateCardPreferenceEngine(
        steps = mutableListOf<ManualRateCardPreferenceFactor>(
            DepotManualRateCardPreferenceAdjuster(
                mutableMapOf<String, Double>("north" to 0.76, "south" to 0.72, "west" to 1.28),
            ),
            CapManualRateCardPreferenceAdjuster(48388.0),
            TieredManualRateCardPreferenceAdjuster(
                mutableListOf<Double>(680.0, 802.0, 1725.0, 3007.0),
                mutableListOf<Double>(1.19, 1.02, 1.05, 1.0),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 55179.1,
    )
}
