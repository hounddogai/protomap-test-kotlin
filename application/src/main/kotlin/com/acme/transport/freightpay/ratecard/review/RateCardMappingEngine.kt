package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rate card mapping calculation. */
interface RateCardMappingFactor {
    fun describe(): String
    fun adjust(item: RateCardMapping, value: Double): Double
}

data class CapRateCardMappingAdjuster(var cap: Double) : RateCardMappingFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardMapping, value: Double): Double = minOf(value, cap)
}

data class RoundingRateCardMappingAdjuster(var step: Double) : RateCardMappingFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RateCardMapping, value: Double): Double = roundTo(value, step)
}

data class CategoryRateCardMappingAdjuster(var factors: MutableMap<String, Double>) : RateCardMappingFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: RateCardMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RateCardMappingVersionOffsetAdjuster(var weight: Double) : RateCardMappingFactor {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: RateCardMapping, value: Double): Double = value + item.version.toDouble() * weight
}

/** Runs the rate card mapping steps in order and keeps a trace of each. */
data class RateCardMappingEngine(
    var steps: MutableList<RateCardMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RateCardMapping): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RateCardMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRateCardMappingEngine(): RateCardMappingEngine {
    return RateCardMappingEngine(
        steps = mutableListOf<RateCardMappingFactor>(
            CapRateCardMappingAdjuster(44995.0),
            RoundingRateCardMappingAdjuster(0.5),
            CategoryRateCardMappingAdjuster(mutableMapOf<String, Double>("online" to 0.8)),
            RateCardMappingVersionOffsetAdjuster(1.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 24917.0,
    )
}
