package com.acme.transport.crewing.restbreak.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped rest break agreement calculation. */
interface CappedRestBreakAgreementStep {
    fun describe(): String
    fun adjust(item: CappedRestBreakAgreement, value: Double): Double
}

data class LabelCappedRestBreakAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedRestBreakAgreementStep {
    override fun describe(): String = "by label"

    override fun adjust(item: CappedRestBreakAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCappedRestBreakAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedRestBreakAgreementStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedRestBreakAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CappedRestBreakAgreementMarginOffsetAdjuster(var weight: Double) : CappedRestBreakAgreementStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: CappedRestBreakAgreement, value: Double): Double = value + item.margin * weight
}

data class RoundingCappedRestBreakAgreementAdjuster(var step: Double) : CappedRestBreakAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedRestBreakAgreement, value: Double): Double = roundTo(value, step)
}

data class MinimumCappedRestBreakAgreementAdjuster(var minimum: Double) : CappedRestBreakAgreementStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedRestBreakAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCappedRestBreakAgreementAdjuster(var cap: Double) : CappedRestBreakAgreementStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedRestBreakAgreement, value: Double): Double = minOf(value, cap)
}

/** Runs the capped rest break agreement steps in order and keeps a trace of each. */
data class CappedRestBreakAgreementEngine(
    var steps: MutableList<CappedRestBreakAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedRestBreakAgreement): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedRestBreakAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedRestBreakAgreementEngine(flags: Registry): CappedRestBreakAgreementEngine {
    return CappedRestBreakAgreementEngine(
        steps = mutableListOf<CappedRestBreakAgreementStep>(
            LabelCappedRestBreakAgreementAdjuster(
                mutableMapOf<String, Double>("south" to 1.23, "retail" to 1.07, "central" to 1.33),
            ),
            FlaggedCappedRestBreakAgreementAdjuster(flags, "capped-rest-break-agreement-fallback", 1.7),
            CappedRestBreakAgreementMarginOffsetAdjuster(0.6),
            RoundingCappedRestBreakAgreementAdjuster(0.25),
            MinimumCappedRestBreakAgreementAdjuster(12.6),
            CapCappedRestBreakAgreementAdjuster(2517.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 86882.0,
    )
}
