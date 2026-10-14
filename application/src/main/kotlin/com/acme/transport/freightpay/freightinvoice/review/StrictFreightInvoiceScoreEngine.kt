package com.acme.transport.freightpay.freightinvoice.review

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict freight invoice score calculation. */
interface StrictFreightInvoiceScoreFactor {
    fun describe(): String
    fun adjust(item: StrictFreightInvoiceScore, value: Double): Double
}

data class RoundingStrictFreightInvoiceScoreAdjuster(var step: Double) : StrictFreightInvoiceScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictFreightInvoiceScore, value: Double): Double = roundTo(value, step)
}

data class PercentStrictFreightInvoiceScoreAdjuster(var percent: Double) : StrictFreightInvoiceScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictFreightInvoiceScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredStrictFreightInvoiceScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictFreightInvoiceScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictFreightInvoiceScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class StrictFreightInvoiceScoreUpdatedAtOffsetAdjuster(
    var weight: Double,
) : StrictFreightInvoiceScoreFactor {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: StrictFreightInvoiceScore, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class RegionStrictFreightInvoiceScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictFreightInvoiceScoreFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: StrictFreightInvoiceScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict freight invoice score steps in order and keeps a trace of each. */
data class StrictFreightInvoiceScoreEngine(
    var steps: MutableList<StrictFreightInvoiceScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictFreightInvoiceScore): Double {
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

    fun evaluateAll(items: MutableList<StrictFreightInvoiceScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictFreightInvoiceScoreEngine(): StrictFreightInvoiceScoreEngine {
    return StrictFreightInvoiceScoreEngine(
        steps = mutableListOf<StrictFreightInvoiceScoreFactor>(
            RoundingStrictFreightInvoiceScoreAdjuster(1.0),
            PercentStrictFreightInvoiceScoreAdjuster(-3.4),
            TieredStrictFreightInvoiceScoreAdjuster(
                mutableListOf<Double>(3587.0, 5409.0, 6804.0, 7988.0),
                mutableListOf<Double>(1.05, 1.08, 0.9, 1.18),
            ),
            StrictFreightInvoiceScoreUpdatedAtOffsetAdjuster(1.78),
            RegionStrictFreightInvoiceScoreAdjuster(
                mutableMapOf<String, Double>("north" to 1.34, "central" to 0.8),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 43335.1,
    )
}
