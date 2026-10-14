package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy qualification policy calculation. */
interface LegacyQualificationPolicyStep {
    fun describe(): String
    fun adjust(item: LegacyQualificationPolicy, value: Double): Double
}

data class RoundingLegacyQualificationPolicyAdjuster(var step: Double) : LegacyQualificationPolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyQualificationPolicy, value: Double): Double = roundTo(value, step)
}

data class TieredLegacyQualificationPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyQualificationPolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyQualificationPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLegacyQualificationPolicyAdjuster(var cap: Double) : LegacyQualificationPolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyQualificationPolicy, value: Double): Double = minOf(value, cap)
}

data class MinimumLegacyQualificationPolicyAdjuster(var minimum: Double) : LegacyQualificationPolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyQualificationPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLegacyQualificationPolicyAdjuster(var percent: Double) : LegacyQualificationPolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyQualificationPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLegacyQualificationPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyQualificationPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyQualificationPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy qualification policy steps in order and keeps a trace of each. */
data class LegacyQualificationPolicyEngine(
    var steps: MutableList<LegacyQualificationPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyQualificationPolicy): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyQualificationPolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyQualificationPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyQualificationPolicyEngine(flags: Registry): LegacyQualificationPolicyEngine {
    return LegacyQualificationPolicyEngine(
        steps = mutableListOf<LegacyQualificationPolicyStep>(
            RoundingLegacyQualificationPolicyAdjuster(0.1),
            TieredLegacyQualificationPolicyAdjuster(
                mutableListOf<Double>(2076.0, 2780.0, 7312.0),
                mutableListOf<Double>(1.13, 0.86, 1.18),
            ),
            CapLegacyQualificationPolicyAdjuster(43314.0),
            MinimumLegacyQualificationPolicyAdjuster(21.7),
            PercentLegacyQualificationPolicyAdjuster(3.0),
            FlaggedLegacyQualificationPolicyAdjuster(flags, "legacy-qualification-policy-pending", 7.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 99318.4,
    )
}
