package com.acme.transport.freightpay.audit.draft

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal audit draft calculation. */
interface SeasonalAuditDraftStep {
    fun describe(): String
    fun adjust(item: SeasonalAuditDraft, value: Double): Double
}

data class PercentSeasonalAuditDraftAdjuster(var percent: Double) : SeasonalAuditDraftStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalAuditDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumSeasonalAuditDraftAdjuster(var minimum: Double) : SeasonalAuditDraftStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalAuditDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredSeasonalAuditDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalAuditDraftStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalAuditDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapSeasonalAuditDraftAdjuster(var cap: Double) : SeasonalAuditDraftStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalAuditDraft, value: Double): Double = minOf(value, cap)
}

data class SeasonalAuditDraftDiscountOffsetAdjuster(var weight: Double) : SeasonalAuditDraftStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: SeasonalAuditDraft, value: Double): Double = value + item.discount * weight
}

data class RoundingSeasonalAuditDraftAdjuster(var step: Double) : SeasonalAuditDraftStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalAuditDraft, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal audit draft steps in order and keeps a trace of each. */
data class SeasonalAuditDraftEngine(
    var steps: MutableList<SeasonalAuditDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalAuditDraft): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalAuditDraftStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalAuditDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalAuditDraftEngine(): SeasonalAuditDraftEngine {
    return SeasonalAuditDraftEngine(
        steps = mutableListOf<SeasonalAuditDraftStep>(
            PercentSeasonalAuditDraftAdjuster(16.7),
            MinimumSeasonalAuditDraftAdjuster(16.7),
            TieredSeasonalAuditDraftAdjuster(
                mutableListOf<Double>(1364.0, 4369.0),
                mutableListOf<Double>(0.85, 0.82),
            ),
            CapSeasonalAuditDraftAdjuster(43938.0),
            SeasonalAuditDraftDiscountOffsetAdjuster(1.26),
            RoundingSeasonalAuditDraftAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 58028.5,
    )
}
