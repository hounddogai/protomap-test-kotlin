package com.acme.transport.freightpay.ratecard.score

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default rate card policy calculation. */
interface DefaultRateCardPolicyFactor {
    fun describe(): String
    fun adjust(item: DefaultRateCardPolicy, value: Double): Double
}

data class MinimumDefaultRateCardPolicyAdjuster(var minimum: Double) : DefaultRateCardPolicyFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultRateCardPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelDefaultRateCardPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultRateCardPolicyFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: DefaultRateCardPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDefaultRateCardPolicyAdjuster(var percent: Double) : DefaultRateCardPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultRateCardPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDefaultRateCardPolicyAdjuster(var cap: Double) : DefaultRateCardPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultRateCardPolicy, value: Double): Double = minOf(value, cap)
}

data class RoundingDefaultRateCardPolicyAdjuster(var step: Double) : DefaultRateCardPolicyFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultRateCardPolicy, value: Double): Double = roundTo(value, step)
}

data class DefaultRateCardPolicyDiscountOffsetAdjuster(var weight: Double) : DefaultRateCardPolicyFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: DefaultRateCardPolicy, value: Double): Double = value + item.discount * weight
}

/** Runs the default rate card policy steps in order and keeps a trace of each. */
data class DefaultRateCardPolicyEngine(
    var steps: MutableList<DefaultRateCardPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultRateCardPolicy): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultRateCardPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultRateCardPolicyEngine(): DefaultRateCardPolicyEngine {
    return DefaultRateCardPolicyEngine(
        steps = mutableListOf<DefaultRateCardPolicyFactor>(
            MinimumDefaultRateCardPolicyAdjuster(30.3),
            LabelDefaultRateCardPolicyAdjuster(mutableMapOf<String, Double>("south" to 1.31)),
            PercentDefaultRateCardPolicyAdjuster(-23.0),
            CapDefaultRateCardPolicyAdjuster(31044.0),
            RoundingDefaultRateCardPolicyAdjuster(0.5),
            DefaultRateCardPolicyDiscountOffsetAdjuster(1.09),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 84155.3,
    )
}
