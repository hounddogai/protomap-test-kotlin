package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive tire agreement calculation. */
interface AdaptiveTireAgreementAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveTireAgreement, value: Double): Double
}

data class FlaggedAdaptiveTireAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveTireAgreementAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveTireAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAdaptiveTireAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveTireAgreementAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveTireAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAdaptiveTireAgreementAdjuster(var cap: Double) : AdaptiveTireAgreementAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveTireAgreement, value: Double): Double = minOf(value, cap)
}

data class MinimumAdaptiveTireAgreementAdjuster(var minimum: Double) : AdaptiveTireAgreementAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveTireAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveTireAgreementAdjuster(var step: Double) : AdaptiveTireAgreementAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveTireAgreement, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive tire agreement steps in order and keeps a trace of each. */
data class AdaptiveTireAgreementEngine(
    var steps: MutableList<AdaptiveTireAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveTireAgreement): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveTireAgreementAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveTireAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveTireAgreementEngine(flags: Registry): AdaptiveTireAgreementEngine {
    return AdaptiveTireAgreementEngine(
        steps = mutableListOf<AdaptiveTireAgreementAdjuster>(
            FlaggedAdaptiveTireAgreementAdjuster(flags, "adaptive-tire-agreement-default", -8.3),
            TieredAdaptiveTireAgreementAdjuster(
                mutableListOf<Double>(6438.0, 7115.0, 9041.0),
                mutableListOf<Double>(1.04, 1.06, 0.98),
            ),
            CapAdaptiveTireAgreementAdjuster(8103.0),
            MinimumAdaptiveTireAgreementAdjuster(2.1),
            RoundingAdaptiveTireAgreementAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 58579.7,
    )
}
