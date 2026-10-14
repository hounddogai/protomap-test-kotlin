package com.acme.transport.freightpay.ratecard.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient rate card profile calculation. */
interface LenientRateCardProfileModifier {
    fun describe(): String
    fun adjust(item: LenientRateCardProfile, value: Double): Double
}

data class FlaggedLenientRateCardProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientRateCardProfileModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientRateCardProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLenientRateCardProfileAdjuster(var minimum: Double) : LenientRateCardProfileModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientRateCardProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLenientRateCardProfileAdjuster(var step: Double) : LenientRateCardProfileModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientRateCardProfile, value: Double): Double = roundTo(value, step)
}

/** Runs the lenient rate card profile steps in order and keeps a trace of each. */
data class LenientRateCardProfileEngine(
    var steps: MutableList<LenientRateCardProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientRateCardProfile): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientRateCardProfileModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LenientRateCardProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientRateCardProfileEngine(flags: Registry): LenientRateCardProfileEngine {
    return LenientRateCardProfileEngine(
        steps = mutableListOf<LenientRateCardProfileModifier>(
            FlaggedLenientRateCardProfileAdjuster(flags, "lenient-rate-card-profile-strict", -5.5),
            MinimumLenientRateCardProfileAdjuster(30.2),
            RoundingLenientRateCardProfileAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 22361.3,
    )
}
