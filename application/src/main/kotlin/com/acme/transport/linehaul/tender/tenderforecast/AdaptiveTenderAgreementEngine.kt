package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive tender agreement calculation. */
interface AdaptiveTenderAgreementFactor {
    fun describe(): String
    fun adjust(item: AdaptiveTenderAgreement, value: Double): Double
}

data class AdaptiveTenderAgreementRatioOffsetAdjuster(var weight: Double) : AdaptiveTenderAgreementFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: AdaptiveTenderAgreement, value: Double): Double = value + item.ratio * weight
}

data class TitleAdaptiveTenderAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveTenderAgreementFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveTenderAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedAdaptiveTenderAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveTenderAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveTenderAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive tender agreement steps in order and keeps a trace of each. */
data class AdaptiveTenderAgreementEngine(
    var steps: MutableList<AdaptiveTenderAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveTenderAgreement): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveTenderAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveTenderAgreementEngine(flags: Registry): AdaptiveTenderAgreementEngine {
    return AdaptiveTenderAgreementEngine(
        steps = mutableListOf<AdaptiveTenderAgreementFactor>(
            AdaptiveTenderAgreementRatioOffsetAdjuster(1.14),
            TitleAdaptiveTenderAgreementAdjuster(
                mutableMapOf<String, Double>("south" to 0.82, "online" to 1.22),
            ),
            FlaggedAdaptiveTenderAgreementAdjuster(flags, "adaptive-tender-agreement-weighted", -5.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 20180.9,
    )
}
