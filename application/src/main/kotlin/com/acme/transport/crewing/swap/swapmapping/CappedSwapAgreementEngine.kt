package com.acme.transport.crewing.swap.swapmapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped swap agreement calculation. */
interface CappedSwapAgreementModifier {
    fun describe(): String
    fun adjust(item: CappedSwapAgreement, value: Double): Double
}

data class PercentCappedSwapAgreementAdjuster(var percent: Double) : CappedSwapAgreementModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedSwapAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCappedSwapAgreementAdjuster(var cap: Double) : CappedSwapAgreementModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedSwapAgreement, value: Double): Double = minOf(value, cap)
}

data class FlaggedCappedSwapAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedSwapAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedSwapAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCappedSwapAgreementAdjuster(var minimum: Double) : CappedSwapAgreementModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedSwapAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedSwapAgreementAdjuster(var step: Double) : CappedSwapAgreementModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedSwapAgreement, value: Double): Double = roundTo(value, step)
}

/** Runs the capped swap agreement steps in order and keeps a trace of each. */
data class CappedSwapAgreementEngine(
    var steps: MutableList<CappedSwapAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedSwapAgreement): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedSwapAgreementModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedSwapAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedSwapAgreementEngine(flags: Registry): CappedSwapAgreementEngine {
    return CappedSwapAgreementEngine(
        steps = mutableListOf<CappedSwapAgreementModifier>(
            PercentCappedSwapAgreementAdjuster(-6.0),
            CapCappedSwapAgreementAdjuster(41736.0),
            FlaggedCappedSwapAgreementAdjuster(flags, "capped-swap-agreement-capped", 8.6),
            MinimumCappedSwapAgreementAdjuster(18.0),
            RoundingCappedSwapAgreementAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 77911.9,
    )
}
