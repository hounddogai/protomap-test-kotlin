package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global qualification preference calculation. */
interface GlobalQualificationPreferenceStep {
    fun describe(): String
    fun adjust(item: GlobalQualificationPreference, value: Double): Double
}

data class NameGlobalQualificationPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalQualificationPreferenceStep {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalQualificationPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentGlobalQualificationPreferenceAdjuster(
    var percent: Double,
) : GlobalQualificationPreferenceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalQualificationPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedGlobalQualificationPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalQualificationPreferenceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalQualificationPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalQualificationPreferenceAdjuster(var step: Double) : GlobalQualificationPreferenceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalQualificationPreference, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalQualificationPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalQualificationPreferenceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalQualificationPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global qualification preference steps in order and keeps a trace of each. */
data class GlobalQualificationPreferenceEngine(
    var steps: MutableList<GlobalQualificationPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalQualificationPreference): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalQualificationPreferenceStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalQualificationPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalQualificationPreferenceEngine(flags: Registry): GlobalQualificationPreferenceEngine {
    return GlobalQualificationPreferenceEngine(
        steps = mutableListOf<GlobalQualificationPreferenceStep>(
            NameGlobalQualificationPreferenceAdjuster(
                mutableMapOf<String, Double>("central" to 0.99, "west" to 1.1, "retail" to 0.86),
            ),
            PercentGlobalQualificationPreferenceAdjuster(6.4),
            FlaggedGlobalQualificationPreferenceAdjuster(flags, "global-qualification-preference-pending", 3.6),
            RoundingGlobalQualificationPreferenceAdjuster(0.25),
            TieredGlobalQualificationPreferenceAdjuster(
                mutableListOf<Double>(1868.0, 4268.0, 5169.0, 5634.0),
                mutableListOf<Double>(1.0, 0.84, 0.93, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 22570.2,
    )
}
