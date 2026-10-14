package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted ev charge mapping calculation. */
interface WeightedEvChargeMappingModifier {
    fun describe(): String
    fun adjust(item: WeightedEvChargeMapping, value: Double): Double
}

data class RoundingWeightedEvChargeMappingAdjuster(var step: Double) : WeightedEvChargeMappingModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedEvChargeMapping, value: Double): Double = roundTo(value, step)
}

data class PercentWeightedEvChargeMappingAdjuster(var percent: Double) : WeightedEvChargeMappingModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedEvChargeMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapWeightedEvChargeMappingAdjuster(var cap: Double) : WeightedEvChargeMappingModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedEvChargeMapping, value: Double): Double = minOf(value, cap)
}

data class WeightedEvChargeMappingCapacityOffsetAdjuster(var weight: Double) : WeightedEvChargeMappingModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: WeightedEvChargeMapping, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class TieredWeightedEvChargeMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedEvChargeMappingModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedEvChargeMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted ev charge mapping steps in order and keeps a trace of each. */
data class WeightedEvChargeMappingEngine(
    var steps: MutableList<WeightedEvChargeMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedEvChargeMapping): Double {
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

    fun evaluateAll(items: MutableList<WeightedEvChargeMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedEvChargeMappingEngine(): WeightedEvChargeMappingEngine {
    return WeightedEvChargeMappingEngine(
        steps = mutableListOf<WeightedEvChargeMappingModifier>(
            RoundingWeightedEvChargeMappingAdjuster(1.0),
            PercentWeightedEvChargeMappingAdjuster(18.2),
            CapWeightedEvChargeMappingAdjuster(4166.0),
            WeightedEvChargeMappingCapacityOffsetAdjuster(0.34),
            TieredWeightedEvChargeMappingAdjuster(
                mutableListOf<Double>(138.0, 2979.0, 5474.0, 6019.0),
                mutableListOf<Double>(1.1, 1.05, 1.05, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 90955.3,
    )
}
