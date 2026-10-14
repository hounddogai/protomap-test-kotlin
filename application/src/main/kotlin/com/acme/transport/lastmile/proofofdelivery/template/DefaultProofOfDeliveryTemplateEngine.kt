package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default proof of delivery template calculation. */
interface DefaultProofOfDeliveryTemplateAdjuster {
    fun describe(): String
    fun adjust(item: DefaultProofOfDeliveryTemplate, value: Double): Double
}

data class RoundingDefaultProofOfDeliveryTemplateAdjuster(
    var step: Double,
) : DefaultProofOfDeliveryTemplateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultProofOfDeliveryTemplate, value: Double): Double = roundTo(value, step)
}

data class PercentDefaultProofOfDeliveryTemplateAdjuster(
    var percent: Double,
) : DefaultProofOfDeliveryTemplateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultProofOfDeliveryTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDefaultProofOfDeliveryTemplateAdjuster(var cap: Double) : DefaultProofOfDeliveryTemplateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultProofOfDeliveryTemplate, value: Double): Double = minOf(value, cap)
}

data class MinimumDefaultProofOfDeliveryTemplateAdjuster(
    var minimum: Double,
) : DefaultProofOfDeliveryTemplateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultProofOfDeliveryTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class UnitDefaultProofOfDeliveryTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultProofOfDeliveryTemplateAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: DefaultProofOfDeliveryTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the default proof of delivery template steps in order and keeps a trace of each. */
data class DefaultProofOfDeliveryTemplateEngine(
    var steps: MutableList<DefaultProofOfDeliveryTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultProofOfDeliveryTemplate): Double {
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

    fun evaluateAll(items: MutableList<DefaultProofOfDeliveryTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultProofOfDeliveryTemplateEngine(): DefaultProofOfDeliveryTemplateEngine {
    return DefaultProofOfDeliveryTemplateEngine(
        steps = mutableListOf<DefaultProofOfDeliveryTemplateAdjuster>(
            RoundingDefaultProofOfDeliveryTemplateAdjuster(0.5),
            PercentDefaultProofOfDeliveryTemplateAdjuster(0.1),
            CapDefaultProofOfDeliveryTemplateAdjuster(19298.0),
            MinimumDefaultProofOfDeliveryTemplateAdjuster(41.0),
            UnitDefaultProofOfDeliveryTemplateAdjuster(mutableMapOf<String, Double>("central" to 1.26)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 38169.2,
    )
}
