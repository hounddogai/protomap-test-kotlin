package com.acme.transport.lastmile.locker.forecast

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual locker preference calculation. */
interface ManualLockerPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: ManualLockerPreference, value: Double): Double
}

data class ManualLockerPreferencePalletsOffsetAdjuster(var weight: Double) : ManualLockerPreferenceAdjuster {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: ManualLockerPreference, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class TitleManualLockerPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualLockerPreferenceAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualLockerPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapManualLockerPreferenceAdjuster(var cap: Double) : ManualLockerPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualLockerPreference, value: Double): Double = minOf(value, cap)
}

data class PercentManualLockerPreferenceAdjuster(var percent: Double) : ManualLockerPreferenceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualLockerPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredManualLockerPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualLockerPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualLockerPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumManualLockerPreferenceAdjuster(var minimum: Double) : ManualLockerPreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualLockerPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual locker preference steps in order and keeps a trace of each. */
data class ManualLockerPreferenceEngine(
    var steps: MutableList<ManualLockerPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualLockerPreference): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualLockerPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualLockerPreferenceEngine(): ManualLockerPreferenceEngine {
    return ManualLockerPreferenceEngine(
        steps = mutableListOf<ManualLockerPreferenceAdjuster>(
            ManualLockerPreferencePalletsOffsetAdjuster(1.01),
            TitleManualLockerPreferenceAdjuster(mutableMapOf<String, Double>("retail" to 0.8)),
            CapManualLockerPreferenceAdjuster(6266.0),
            PercentManualLockerPreferenceAdjuster(7.7),
            TieredManualLockerPreferenceAdjuster(
                mutableListOf<Double>(279.0, 9401.0),
                mutableListOf<Double>(1.08, 1.02),
            ),
            MinimumManualLockerPreferenceAdjuster(17.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 91319.6,
    )
}
