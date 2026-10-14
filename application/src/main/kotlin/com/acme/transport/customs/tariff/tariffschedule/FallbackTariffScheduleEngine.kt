package com.acme.transport.customs.tariff.tariffschedule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback tariff schedule calculation. */
interface FallbackTariffScheduleModifier {
    fun describe(): String
    fun adjust(item: FallbackTariffSchedule, value: Double): Double
}

data class MinimumFallbackTariffScheduleAdjuster(var minimum: Double) : FallbackTariffScheduleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackTariffSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentFallbackTariffScheduleAdjuster(var percent: Double) : FallbackTariffScheduleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackTariffSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFallbackTariffScheduleAdjuster(var step: Double) : FallbackTariffScheduleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackTariffSchedule, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback tariff schedule steps in order and keeps a trace of each. */
data class FallbackTariffScheduleEngine(
    var steps: MutableList<FallbackTariffScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackTariffSchedule): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackTariffSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackTariffScheduleEngine(): FallbackTariffScheduleEngine {
    return FallbackTariffScheduleEngine(
        steps = mutableListOf<FallbackTariffScheduleModifier>(
            MinimumFallbackTariffScheduleAdjuster(48.3),
            PercentFallbackTariffScheduleAdjuster(14.9),
            RoundingFallbackTariffScheduleAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 44072.3,
    )
}
