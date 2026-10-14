package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.kit.numbers.roundTo

/** One step of the redelivery draft calculation. */
interface RedeliveryDraftFactor {
    fun describe(): String
    fun adjust(item: RedeliveryDraft, value: Double): Double
}

data class MinimumRedeliveryDraftAdjuster(var minimum: Double) : RedeliveryDraftFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRedeliveryDraftAdjuster(var step: Double) : RedeliveryDraftFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryDraft, value: Double): Double = roundTo(value, step)
}

data class PercentRedeliveryDraftAdjuster(var percent: Double) : RedeliveryDraftFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRedeliveryDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RedeliveryDraftFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RedeliveryDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the redelivery draft steps in order and keeps a trace of each. */
data class RedeliveryDraftEngine(
    var steps: MutableList<RedeliveryDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RedeliveryDraft): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryDraftFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRedeliveryDraftEngine(): RedeliveryDraftEngine {
    return RedeliveryDraftEngine(
        steps = mutableListOf<RedeliveryDraftFactor>(
            MinimumRedeliveryDraftAdjuster(30.7),
            RoundingRedeliveryDraftAdjuster(0.5),
            PercentRedeliveryDraftAdjuster(7.5),
            TieredRedeliveryDraftAdjuster(
                mutableListOf<Double>(1643.0, 3839.0),
                mutableListOf<Double>(0.88, 1.07),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 51654.7,
    )
}
