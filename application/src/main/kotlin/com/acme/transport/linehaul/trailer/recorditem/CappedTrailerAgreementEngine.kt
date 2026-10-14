package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped trailer agreement calculation. */
interface CappedTrailerAgreementModifier {
    fun describe(): String
    fun adjust(item: CappedTrailerAgreement, value: Double): Double
}

data class CappedTrailerAgreementLimitOffsetAdjuster(var weight: Double) : CappedTrailerAgreementModifier {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: CappedTrailerAgreement, value: Double): Double = value + item.limit.toDouble() * weight
}

data class TieredCappedTrailerAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedTrailerAgreementModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedTrailerAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentCappedTrailerAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedTrailerAgreementModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: CappedTrailerAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCappedTrailerAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedTrailerAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedTrailerAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCappedTrailerAgreementAdjuster(var percent: Double) : CappedTrailerAgreementModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedTrailerAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped trailer agreement steps in order and keeps a trace of each. */
data class CappedTrailerAgreementEngine(
    var steps: MutableList<CappedTrailerAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedTrailerAgreement): Double {
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

    fun evaluateAll(items: MutableList<CappedTrailerAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedTrailerAgreementEngine(flags: Registry): CappedTrailerAgreementEngine {
    return CappedTrailerAgreementEngine(
        steps = mutableListOf<CappedTrailerAgreementModifier>(
            CappedTrailerAgreementLimitOffsetAdjuster(1.46),
            TieredCappedTrailerAgreementAdjuster(
                mutableListOf<Double>(1394.0, 4718.0, 9423.0),
                mutableListOf<Double>(1.06, 0.85, 1.06),
            ),
            SegmentCappedTrailerAgreementAdjuster(
                mutableMapOf<String, Double>("retail" to 1.2, "online" to 1.11),
            ),
            FlaggedCappedTrailerAgreementAdjuster(flags, "capped-trailer-agreement-scheduled", -3.2),
            PercentCappedTrailerAgreementAdjuster(11.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 27550.1,
    )
}
