package com.acme.transport.freightpay.freightinvoice.score

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the freight invoice score calculation. */
interface FreightInvoiceScoreFactor {
    fun describe(): String
    fun adjust(item: FreightInvoiceScore, value: Double): Double
}

data class FreightInvoiceScoreBudgetOffsetAdjuster(var weight: Double) : FreightInvoiceScoreFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: FreightInvoiceScore, value: Double): Double = value + item.budget * weight
}

data class RoundingFreightInvoiceScoreAdjuster(var step: Double) : FreightInvoiceScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FreightInvoiceScore, value: Double): Double = roundTo(value, step)
}

data class PercentFreightInvoiceScoreAdjuster(var percent: Double) : FreightInvoiceScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FreightInvoiceScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredFreightInvoiceScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FreightInvoiceScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FreightInvoiceScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumFreightInvoiceScoreAdjuster(var minimum: Double) : FreightInvoiceScoreFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FreightInvoiceScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the freight invoice score steps in order and keeps a trace of each. */
data class FreightInvoiceScoreEngine(
    var steps: MutableList<FreightInvoiceScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FreightInvoiceScore): Double {
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

    fun evaluateAll(items: MutableList<FreightInvoiceScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFreightInvoiceScoreEngine(): FreightInvoiceScoreEngine {
    return FreightInvoiceScoreEngine(
        steps = mutableListOf<FreightInvoiceScoreFactor>(
            FreightInvoiceScoreBudgetOffsetAdjuster(1.93),
            RoundingFreightInvoiceScoreAdjuster(0.1),
            PercentFreightInvoiceScoreAdjuster(-18.9),
            TieredFreightInvoiceScoreAdjuster(
                mutableListOf<Double>(4120.0, 7981.0, 9215.0, 9527.0),
                mutableListOf<Double>(0.97, 0.9, 0.88, 0.92),
            ),
            MinimumFreightInvoiceScoreAdjuster(7.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 79774.2,
    )
}
