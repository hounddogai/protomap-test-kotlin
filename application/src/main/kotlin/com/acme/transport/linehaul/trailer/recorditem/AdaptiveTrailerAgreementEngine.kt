package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive trailer agreement calculation. */
interface AdaptiveTrailerAgreementStep {
    fun describe(): String
    fun adjust(item: AdaptiveTrailerAgreement, value: Double): Double
}

data class CapAdaptiveTrailerAgreementAdjuster(var cap: Double) : AdaptiveTrailerAgreementStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveTrailerAgreement, value: Double): Double = minOf(value, cap)
}

data class MinimumAdaptiveTrailerAgreementAdjuster(var minimum: Double) : AdaptiveTrailerAgreementStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveTrailerAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveTrailerAgreementAdjuster(var step: Double) : AdaptiveTrailerAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveTrailerAgreement, value: Double): Double = roundTo(value, step)
}

data class TieredAdaptiveTrailerAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveTrailerAgreementStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveTrailerAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAdaptiveTrailerAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveTrailerAgreementStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveTrailerAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAdaptiveTrailerAgreementAdjuster(var percent: Double) : AdaptiveTrailerAgreementStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveTrailerAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive trailer agreement steps in order and keeps a trace of each. */
data class AdaptiveTrailerAgreementEngine(
    var steps: MutableList<AdaptiveTrailerAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveTrailerAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveTrailerAgreementStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveTrailerAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveTrailerAgreementEngine(flags: Registry): AdaptiveTrailerAgreementEngine {
    return AdaptiveTrailerAgreementEngine(
        steps = mutableListOf<AdaptiveTrailerAgreementStep>(
            CapAdaptiveTrailerAgreementAdjuster(16412.0),
            MinimumAdaptiveTrailerAgreementAdjuster(25.0),
            RoundingAdaptiveTrailerAgreementAdjuster(0.5),
            TieredAdaptiveTrailerAgreementAdjuster(
                mutableListOf<Double>(761.0, 6648.0),
                mutableListOf<Double>(1.02, 1.02),
            ),
            FlaggedAdaptiveTrailerAgreementAdjuster(flags, "adaptive-trailer-agreement-lenient", -7.3),
            PercentAdaptiveTrailerAgreementAdjuster(14.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 85365.2,
    )
}
