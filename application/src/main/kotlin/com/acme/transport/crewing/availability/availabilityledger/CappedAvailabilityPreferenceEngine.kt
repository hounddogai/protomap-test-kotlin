package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped availability preference calculation. */
interface CappedAvailabilityPreferenceFactor {
    fun describe(): String
    fun adjust(item: CappedAvailabilityPreference, value: Double): Double
}

data class CappedAvailabilityPreferenceQuantityOffsetAdjuster(
    var weight: Double,
) : CappedAvailabilityPreferenceFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: CappedAvailabilityPreference, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class RoundingCappedAvailabilityPreferenceAdjuster(var step: Double) : CappedAvailabilityPreferenceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedAvailabilityPreference, value: Double): Double = roundTo(value, step)
}

data class RegionCappedAvailabilityPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedAvailabilityPreferenceFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: CappedAvailabilityPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCappedAvailabilityPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedAvailabilityPreferenceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedAvailabilityPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCappedAvailabilityPreferenceAdjuster(
    var percent: Double,
) : CappedAvailabilityPreferenceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedAvailabilityPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped availability preference steps in order and keeps a trace of each. */
data class CappedAvailabilityPreferenceEngine(
    var steps: MutableList<CappedAvailabilityPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedAvailabilityPreference): Double {
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

    fun evaluateAll(items: MutableList<CappedAvailabilityPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedAvailabilityPreferenceEngine(flags: Registry): CappedAvailabilityPreferenceEngine {
    return CappedAvailabilityPreferenceEngine(
        steps = mutableListOf<CappedAvailabilityPreferenceFactor>(
            CappedAvailabilityPreferenceQuantityOffsetAdjuster(1.17),
            RoundingCappedAvailabilityPreferenceAdjuster(0.5),
            RegionCappedAvailabilityPreferenceAdjuster(
                mutableMapOf<String, Double>("east" to 0.76, "west" to 1.14, "north" to 0.97),
            ),
            FlaggedCappedAvailabilityPreferenceAdjuster(flags, "capped-availability-preference-lenient", 8.7),
            PercentCappedAvailabilityPreferenceAdjuster(-24.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 70789.5,
    )
}
