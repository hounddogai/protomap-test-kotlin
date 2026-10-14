package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.flags.Registry

/** One step of the lenient near miss agreement calculation. */
interface LenientNearMissAgreementModifier {
    fun describe(): String
    fun adjust(item: LenientNearMissAgreement, value: Double): Double
}

data class MinimumLenientNearMissAgreementAdjuster(var minimum: Double) : LenientNearMissAgreementModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientNearMissAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientNearMissAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientNearMissAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientNearMissAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class UnitLenientNearMissAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientNearMissAgreementModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: LenientNearMissAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLenientNearMissAgreementAdjuster(var percent: Double) : LenientNearMissAgreementModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientNearMissAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLenientNearMissAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientNearMissAgreementModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientNearMissAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient near miss agreement steps in order and keeps a trace of each. */
data class LenientNearMissAgreementEngine(
    var steps: MutableList<LenientNearMissAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientNearMissAgreement): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientNearMissAgreementModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LenientNearMissAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientNearMissAgreementEngine(flags: Registry): LenientNearMissAgreementEngine {
    return LenientNearMissAgreementEngine(
        steps = mutableListOf<LenientNearMissAgreementModifier>(
            MinimumLenientNearMissAgreementAdjuster(24.9),
            FlaggedLenientNearMissAgreementAdjuster(flags, "lenient-near-miss-agreement-rolling", 8.2),
            UnitLenientNearMissAgreementAdjuster(
                mutableMapOf<String, Double>("north" to 1.26, "retail" to 1.25),
            ),
            PercentLenientNearMissAgreementAdjuster(19.1),
            TieredLenientNearMissAgreementAdjuster(
                mutableListOf<Double>(5194.0, 7001.0, 8232.0),
                mutableListOf<Double>(1.05, 0.81, 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 37639.8,
    )
}
