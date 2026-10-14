package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped rate card notice calculation. */
interface CappedRateCardNoticeModifier {
    fun describe(): String
    fun adjust(item: CappedRateCardNotice, value: Double): Double
}

data class TieredCappedRateCardNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedRateCardNoticeModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedRateCardNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCappedRateCardNoticeAdjuster(var step: Double) : CappedRateCardNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedRateCardNotice, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedRateCardNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedRateCardNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedRateCardNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleCappedRateCardNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedRateCardNoticeModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: CappedRateCardNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCappedRateCardNoticeAdjuster(var percent: Double) : CappedRateCardNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedRateCardNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped rate card notice steps in order and keeps a trace of each. */
data class CappedRateCardNoticeEngine(
    var steps: MutableList<CappedRateCardNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedRateCardNotice): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedRateCardNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedRateCardNoticeEngine(flags: Registry): CappedRateCardNoticeEngine {
    return CappedRateCardNoticeEngine(
        steps = mutableListOf<CappedRateCardNoticeModifier>(
            TieredCappedRateCardNoticeAdjuster(
                mutableListOf<Double>(1836.0, 8532.0, 9411.0, 9498.0),
                mutableListOf<Double>(0.85, 1.08, 0.94, 0.98),
            ),
            RoundingCappedRateCardNoticeAdjuster(5.0),
            FlaggedCappedRateCardNoticeAdjuster(flags, "capped-rate-card-notice-primary", 7.2),
            TitleCappedRateCardNoticeAdjuster(mutableMapOf<String, Double>("north" to 1.37, "central" to 1.32)),
            PercentCappedRateCardNoticeAdjuster(15.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 78678.5,
    )
}
