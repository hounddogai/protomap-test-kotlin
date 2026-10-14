package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped hub mapping calculation. */
interface CappedHubMappingStep {
    fun describe(): String
    fun adjust(item: CappedHubMapping, value: Double): Double
}

data class TieredCappedHubMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedHubMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedHubMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCappedHubMappingAdjuster(var step: Double) : CappedHubMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedHubMapping, value: Double): Double = roundTo(value, step)
}

data class CountryCappedHubMappingAdjuster(var factors: MutableMap<String, Double>) : CappedHubMappingStep {
    override fun describe(): String = "by country"

    override fun adjust(item: CappedHubMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CappedHubMappingDiscountOffsetAdjuster(var weight: Double) : CappedHubMappingStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: CappedHubMapping, value: Double): Double = value + item.discount * weight
}

data class CapCappedHubMappingAdjuster(var cap: Double) : CappedHubMappingStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedHubMapping, value: Double): Double = minOf(value, cap)
}

/** Runs the capped hub mapping steps in order and keeps a trace of each. */
data class CappedHubMappingEngine(
    var steps: MutableList<CappedHubMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedHubMapping): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedHubMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedHubMappingEngine(): CappedHubMappingEngine {
    return CappedHubMappingEngine(
        steps = mutableListOf<CappedHubMappingStep>(
            TieredCappedHubMappingAdjuster(
                mutableListOf<Double>(4739.0, 9574.0),
                mutableListOf<Double>(0.97, 0.99),
            ),
            RoundingCappedHubMappingAdjuster(0.5),
            CountryCappedHubMappingAdjuster(
                mutableMapOf<String, Double>("central" to 0.98, "east" to 1.14, "online" to 0.85),
            ),
            CappedHubMappingDiscountOffsetAdjuster(0.63),
            CapCappedHubMappingAdjuster(4801.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 76908.9,
    )
}
