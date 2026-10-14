package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rest break agreement calculation. */
interface RestBreakAgreementModifier {
    fun describe(): String
    fun adjust(item: RestBreakAgreement, value: Double): Double
}

data class MinimumRestBreakAgreementAdjuster(var minimum: Double) : RestBreakAgreementModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRestBreakAgreementAdjuster(var cap: Double) : RestBreakAgreementModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RestBreakAgreement, value: Double): Double = minOf(value, cap)
}

data class TieredRestBreakAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakAgreementModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRestBreakAgreementAdjuster(var step: Double) : RestBreakAgreementModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakAgreement, value: Double): Double = roundTo(value, step)
}

data class PercentRestBreakAgreementAdjuster(var percent: Double) : RestBreakAgreementModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRestBreakAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rest break agreement steps in order and keeps a trace of each. */
data class RestBreakAgreementEngine(
    var steps: MutableList<RestBreakAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RestBreakAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakAgreementModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRestBreakAgreementEngine(flags: Registry): RestBreakAgreementEngine {
    return RestBreakAgreementEngine(
        steps = mutableListOf<RestBreakAgreementModifier>(
            MinimumRestBreakAgreementAdjuster(23.2),
            CapRestBreakAgreementAdjuster(28828.0),
            TieredRestBreakAgreementAdjuster(
                mutableListOf<Double>(493.0, 1886.0, 3818.0, 8416.0),
                mutableListOf<Double>(1.06, 1.13, 1.12, 1.02),
            ),
            RoundingRestBreakAgreementAdjuster(0.5),
            PercentRestBreakAgreementAdjuster(9.3),
            FlaggedRestBreakAgreementAdjuster(flags, "rest-break-agreement-capped", -9.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 65595.3,
    )
}
