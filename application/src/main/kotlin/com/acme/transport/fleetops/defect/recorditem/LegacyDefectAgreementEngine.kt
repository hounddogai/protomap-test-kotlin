package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy defect agreement calculation. */
interface LegacyDefectAgreementModifier {
    fun describe(): String
    fun adjust(item: LegacyDefectAgreement, value: Double): Double
}

data class TieredLegacyDefectAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyDefectAgreementModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyDefectAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLegacyDefectAgreementAdjuster(var step: Double) : LegacyDefectAgreementModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyDefectAgreement, value: Double): Double = roundTo(value, step)
}

data class MinimumLegacyDefectAgreementAdjuster(var minimum: Double) : LegacyDefectAgreementModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyDefectAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLegacyDefectAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyDefectAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyDefectAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLegacyDefectAgreementAdjuster(var percent: Double) : LegacyDefectAgreementModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyDefectAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy defect agreement steps in order and keeps a trace of each. */
data class LegacyDefectAgreementEngine(
    var steps: MutableList<LegacyDefectAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyDefectAgreement): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyDefectAgreementModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyDefectAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyDefectAgreementEngine(flags: Registry): LegacyDefectAgreementEngine {
    return LegacyDefectAgreementEngine(
        steps = mutableListOf<LegacyDefectAgreementModifier>(
            TieredLegacyDefectAgreementAdjuster(
                mutableListOf<Double>(572.0, 2766.0, 7426.0, 9366.0),
                mutableListOf<Double>(1.02, 1.04, 1.15, 0.87),
            ),
            RoundingLegacyDefectAgreementAdjuster(0.25),
            MinimumLegacyDefectAgreementAdjuster(39.9),
            FlaggedLegacyDefectAgreementAdjuster(flags, "legacy-defect-agreement-capped", 0.6),
            PercentLegacyDefectAgreementAdjuster(-23.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 68151.1,
    )
}
