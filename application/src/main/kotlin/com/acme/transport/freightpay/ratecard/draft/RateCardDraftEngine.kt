package com.acme.transport.freightpay.ratecard.draft

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rate card draft calculation. */
interface RateCardDraftFactor {
    fun describe(): String
    fun adjust(item: RateCardDraft, value: Double): Double
}

data class DepotRateCardDraftAdjuster(var factors: MutableMap<String, Double>) : RateCardDraftFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: RateCardDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRateCardDraftAdjuster(var cap: Double) : RateCardDraftFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardDraft, value: Double): Double = minOf(value, cap)
}

data class PercentRateCardDraftAdjuster(var percent: Double) : RateCardDraftFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRateCardDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardDraftFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RateCardDraftCreatedAtOffsetAdjuster(var weight: Double) : RateCardDraftFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: RateCardDraft, value: Double): Double = value + item.createdAt.toDouble() * weight
}

/** Runs the rate card draft steps in order and keeps a trace of each. */
data class RateCardDraftEngine(
    var steps: MutableList<RateCardDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RateCardDraft): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RateCardDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRateCardDraftEngine(): RateCardDraftEngine {
    return RateCardDraftEngine(
        steps = mutableListOf<RateCardDraftFactor>(
            DepotRateCardDraftAdjuster(
                mutableMapOf<String, Double>("central" to 1.29, "north" to 1.27, "east" to 1.06),
            ),
            CapRateCardDraftAdjuster(37900.0),
            PercentRateCardDraftAdjuster(-2.8),
            TieredRateCardDraftAdjuster(
                mutableListOf<Double>(1191.0, 9627.0),
                mutableListOf<Double>(1.14, 0.88),
            ),
            RateCardDraftCreatedAtOffsetAdjuster(1.26),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 19630.4,
    )
}
