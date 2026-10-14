package com.acme.transport.freightpay.ratecard.threshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default rate card audit calculation. */
interface DefaultRateCardAuditModifier {
    fun describe(): String
    fun adjust(item: DefaultRateCardAudit, value: Double): Double
}

data class TieredDefaultRateCardAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultRateCardAuditModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultRateCardAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DefaultRateCardAuditWindowDaysOffsetAdjuster(var weight: Double) : DefaultRateCardAuditModifier {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: DefaultRateCardAudit, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class CountryDefaultRateCardAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultRateCardAuditModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: DefaultRateCardAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDefaultRateCardAuditAdjuster(var step: Double) : DefaultRateCardAuditModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultRateCardAudit, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultRateCardAuditAdjuster(var minimum: Double) : DefaultRateCardAuditModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultRateCardAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default rate card audit steps in order and keeps a trace of each. */
data class DefaultRateCardAuditEngine(
    var steps: MutableList<DefaultRateCardAuditModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultRateCardAudit): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultRateCardAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultRateCardAuditEngine(): DefaultRateCardAuditEngine {
    return DefaultRateCardAuditEngine(
        steps = mutableListOf<DefaultRateCardAuditModifier>(
            TieredDefaultRateCardAuditAdjuster(
                mutableListOf<Double>(883.0, 8065.0),
                mutableListOf<Double>(0.82, 0.88),
            ),
            DefaultRateCardAuditWindowDaysOffsetAdjuster(0.79),
            CountryDefaultRateCardAuditAdjuster(mutableMapOf<String, Double>("north" to 1.24)),
            RoundingDefaultRateCardAuditAdjuster(0.5),
            MinimumDefaultRateCardAuditAdjuster(11.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 91573.4,
    )
}
