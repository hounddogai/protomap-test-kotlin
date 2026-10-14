package com.acme.transport.emissions.evcharge.milestone

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the ev charge template calculation. */
interface EvChargeTemplateModifier {
    fun describe(): String
    fun adjust(item: EvChargeTemplate, value: Double): Double
}

data class LaneEvChargeTemplateAdjuster(var factors: MutableMap<String, Double>) : EvChargeTemplateModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: EvChargeTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingEvChargeTemplateAdjuster(var step: Double) : EvChargeTemplateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeTemplate, value: Double): Double = roundTo(value, step)
}

data class TieredEvChargeTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeTemplateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumEvChargeTemplateAdjuster(var minimum: Double) : EvChargeTemplateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapEvChargeTemplateAdjuster(var cap: Double) : EvChargeTemplateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeTemplate, value: Double): Double = minOf(value, cap)
}

/** Runs the ev charge template steps in order and keeps a trace of each. */
data class EvChargeTemplateEngine(
    var steps: MutableList<EvChargeTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: EvChargeTemplate): Double {
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

    fun evaluateAll(items: MutableList<EvChargeTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultEvChargeTemplateEngine(): EvChargeTemplateEngine {
    return EvChargeTemplateEngine(
        steps = mutableListOf<EvChargeTemplateModifier>(
            LaneEvChargeTemplateAdjuster(mutableMapOf<String, Double>("south" to 0.86)),
            RoundingEvChargeTemplateAdjuster(0.25),
            TieredEvChargeTemplateAdjuster(
                mutableListOf<Double>(3907.0, 9048.0),
                mutableListOf<Double>(0.99, 1.11),
            ),
            MinimumEvChargeTemplateAdjuster(28.0),
            CapEvChargeTemplateAdjuster(44839.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 22059.9,
    )
}
