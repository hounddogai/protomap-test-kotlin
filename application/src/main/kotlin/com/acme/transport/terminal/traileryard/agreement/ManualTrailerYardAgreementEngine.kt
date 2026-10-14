package com.acme.transport.terminal.traileryard.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual trailer yard agreement calculation. */
interface ManualTrailerYardAgreementStep {
    fun describe(): String
    fun adjust(item: ManualTrailerYardAgreement, value: Double): Double
}

data class TieredManualTrailerYardAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualTrailerYardAgreementStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualTrailerYardAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingManualTrailerYardAgreementAdjuster(var step: Double) : ManualTrailerYardAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualTrailerYardAgreement, value: Double): Double = roundTo(value, step)
}

data class MinimumManualTrailerYardAgreementAdjuster(var minimum: Double) : ManualTrailerYardAgreementStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualTrailerYardAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentManualTrailerYardAgreementAdjuster(var percent: Double) : ManualTrailerYardAgreementStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTrailerYardAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapManualTrailerYardAgreementAdjuster(var cap: Double) : ManualTrailerYardAgreementStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTrailerYardAgreement, value: Double): Double = minOf(value, cap)
}

data class FlaggedManualTrailerYardAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualTrailerYardAgreementStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualTrailerYardAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the manual trailer yard agreement steps in order and keeps a trace of each. */
data class ManualTrailerYardAgreementEngine(
    var steps: MutableList<ManualTrailerYardAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualTrailerYardAgreement): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualTrailerYardAgreementStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualTrailerYardAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualTrailerYardAgreementEngine(flags: Registry): ManualTrailerYardAgreementEngine {
    return ManualTrailerYardAgreementEngine(
        steps = mutableListOf<ManualTrailerYardAgreementStep>(
            TieredManualTrailerYardAgreementAdjuster(
                mutableListOf<Double>(1706.0, 7076.0),
                mutableListOf<Double>(1.17, 0.91),
            ),
            RoundingManualTrailerYardAgreementAdjuster(0.05),
            MinimumManualTrailerYardAgreementAdjuster(6.5),
            PercentManualTrailerYardAgreementAdjuster(-7.1),
            CapManualTrailerYardAgreementAdjuster(2812.0),
            FlaggedManualTrailerYardAgreementAdjuster(flags, "manual-trailer-yard-agreement-strict", 4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 40490.4,
    )
}
