package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback tariff cycle calculation. */
interface FallbackTariffCycleStep {
    fun describe(): String
    fun adjust(item: FallbackTariffCycle, value: Double): Double
}

data class FallbackTariffCycleStopCountOffsetAdjuster(var weight: Double) : FallbackTariffCycleStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: FallbackTariffCycle, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class MinimumFallbackTariffCycleAdjuster(var minimum: Double) : FallbackTariffCycleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackTariffCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFallbackTariffCycleAdjuster(var step: Double) : FallbackTariffCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackTariffCycle, value: Double): Double = roundTo(value, step)
}

data class SourceFallbackTariffCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackTariffCycleStep {
    override fun describe(): String = "by source"

    override fun adjust(item: FallbackTariffCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredFallbackTariffCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackTariffCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackTariffCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback tariff cycle steps in order and keeps a trace of each. */
data class FallbackTariffCycleEngine(
    var steps: MutableList<FallbackTariffCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackTariffCycle): Double {
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

    fun evaluateAll(items: MutableList<FallbackTariffCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackTariffCycleEngine(): FallbackTariffCycleEngine {
    return FallbackTariffCycleEngine(
        steps = mutableListOf<FallbackTariffCycleStep>(
            FallbackTariffCycleStopCountOffsetAdjuster(0.76),
            MinimumFallbackTariffCycleAdjuster(34.1),
            RoundingFallbackTariffCycleAdjuster(0.25),
            SourceFallbackTariffCycleAdjuster(mutableMapOf<String, Double>("west" to 1.05)),
            TieredFallbackTariffCycleAdjuster(
                mutableListOf<Double>(1132.0, 8389.0, 8905.0, 9285.0),
                mutableListOf<Double>(0.85, 1.1, 1.09, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 21896.1,
    )
}
