package com.acme.transport.lastmile.drop.profile

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive drop record calculation. */
interface AdaptiveDropRecordModifier {
    fun describe(): String
    fun adjust(item: AdaptiveDropRecord, value: Double): Double
}

data class TieredAdaptiveDropRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveDropRecordModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveDropRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CountryAdaptiveDropRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveDropRecordModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: AdaptiveDropRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAdaptiveDropRecordAdjuster(var step: Double) : AdaptiveDropRecordModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveDropRecord, value: Double): Double = roundTo(value, step)
}

data class CapAdaptiveDropRecordAdjuster(var cap: Double) : AdaptiveDropRecordModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveDropRecord, value: Double): Double = minOf(value, cap)
}

data class PercentAdaptiveDropRecordAdjuster(var percent: Double) : AdaptiveDropRecordModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveDropRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive drop record steps in order and keeps a trace of each. */
data class AdaptiveDropRecordEngine(
    var steps: MutableList<AdaptiveDropRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveDropRecord): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveDropRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveDropRecordEngine(): AdaptiveDropRecordEngine {
    return AdaptiveDropRecordEngine(
        steps = mutableListOf<AdaptiveDropRecordModifier>(
            TieredAdaptiveDropRecordAdjuster(
                mutableListOf<Double>(8208.0, 8695.0, 8731.0),
                mutableListOf<Double>(0.9, 0.88, 1.16),
            ),
            CountryAdaptiveDropRecordAdjuster(mutableMapOf<String, Double>("retail" to 1.25, "online" to 1.35)),
            RoundingAdaptiveDropRecordAdjuster(0.1),
            CapAdaptiveDropRecordAdjuster(27141.0),
            PercentAdaptiveDropRecordAdjuster(-6.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 53970.3,
    )
}
