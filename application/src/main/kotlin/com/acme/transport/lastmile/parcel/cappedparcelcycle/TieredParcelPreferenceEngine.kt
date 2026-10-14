package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered parcel preference calculation. */
interface TieredParcelPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: TieredParcelPreference, value: Double): Double
}

data class MinimumTieredParcelPreferenceAdjuster(var minimum: Double) : TieredParcelPreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredParcelPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredParcelPreferenceStartsAtOffsetAdjuster(var weight: Double) : TieredParcelPreferenceAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: TieredParcelPreference, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class TitleTieredParcelPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredParcelPreferenceAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: TieredParcelPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTieredParcelPreferenceAdjuster(var cap: Double) : TieredParcelPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredParcelPreference, value: Double): Double = minOf(value, cap)
}

data class PercentTieredParcelPreferenceAdjuster(var percent: Double) : TieredParcelPreferenceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredParcelPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTieredParcelPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredParcelPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredParcelPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered parcel preference steps in order and keeps a trace of each. */
data class TieredParcelPreferenceEngine(
    var steps: MutableList<TieredParcelPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredParcelPreference): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredParcelPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredParcelPreferenceEngine(): TieredParcelPreferenceEngine {
    return TieredParcelPreferenceEngine(
        steps = mutableListOf<TieredParcelPreferenceAdjuster>(
            MinimumTieredParcelPreferenceAdjuster(1.2),
            TieredParcelPreferenceStartsAtOffsetAdjuster(1.04),
            TitleTieredParcelPreferenceAdjuster(
                mutableMapOf<String, Double>("north" to 1.34, "west" to 1.04, "online" to 1.37),
            ),
            CapTieredParcelPreferenceAdjuster(967.0),
            PercentTieredParcelPreferenceAdjuster(23.0),
            TieredTieredParcelPreferenceAdjuster(
                mutableListOf<Double>(2511.0, 2630.0),
                mutableListOf<Double>(0.93, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 33936.0,
    )
}
