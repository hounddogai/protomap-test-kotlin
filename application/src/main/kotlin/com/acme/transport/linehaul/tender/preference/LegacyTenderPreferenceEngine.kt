package com.acme.transport.linehaul.tender.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy tender preference calculation. */
interface LegacyTenderPreferenceFactor {
    fun describe(): String
    fun adjust(item: LegacyTenderPreference, value: Double): Double
}

data class RoundingLegacyTenderPreferenceAdjuster(var step: Double) : LegacyTenderPreferenceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyTenderPreference, value: Double): Double = roundTo(value, step)
}

data class LegacyTenderPreferenceWindowDaysOffsetAdjuster(var weight: Double) : LegacyTenderPreferenceFactor {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: LegacyTenderPreference, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class MinimumLegacyTenderPreferenceAdjuster(var minimum: Double) : LegacyTenderPreferenceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyTenderPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CategoryLegacyTenderPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyTenderPreferenceFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: LegacyTenderPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLegacyTenderPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyTenderPreferenceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyTenderPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLegacyTenderPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyTenderPreferenceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyTenderPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy tender preference steps in order and keeps a trace of each. */
data class LegacyTenderPreferenceEngine(
    var steps: MutableList<LegacyTenderPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyTenderPreference): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyTenderPreferenceFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyTenderPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyTenderPreferenceEngine(flags: Registry): LegacyTenderPreferenceEngine {
    return LegacyTenderPreferenceEngine(
        steps = mutableListOf<LegacyTenderPreferenceFactor>(
            RoundingLegacyTenderPreferenceAdjuster(0.5),
            LegacyTenderPreferenceWindowDaysOffsetAdjuster(1.81),
            MinimumLegacyTenderPreferenceAdjuster(49.7),
            CategoryLegacyTenderPreferenceAdjuster(
                mutableMapOf<String, Double>("east" to 0.94, "west" to 1.21, "retail" to 1.34),
            ),
            FlaggedLegacyTenderPreferenceAdjuster(flags, "legacy-tender-preference-lenient", 4.7),
            TieredLegacyTenderPreferenceAdjuster(
                mutableListOf<Double>(719.0, 764.0, 2369.0, 5869.0),
                mutableListOf<Double>(0.86, 1.07, 0.89, 1.08),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 90424.7,
    )
}
