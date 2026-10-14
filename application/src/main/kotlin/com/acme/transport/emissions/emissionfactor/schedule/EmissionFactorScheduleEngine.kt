package com.acme.transport.emissions.emissionfactor.schedule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the emission factor schedule calculation. */
interface EmissionFactorScheduleStep {
    fun describe(): String
    fun adjust(item: EmissionFactorSchedule, value: Double): Double
}

data class TieredEmissionFactorScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorScheduleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleEmissionFactorScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorScheduleStep {
    override fun describe(): String = "by title"

    override fun adjust(item: EmissionFactorSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentEmissionFactorScheduleAdjuster(var percent: Double) : EmissionFactorScheduleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingEmissionFactorScheduleAdjuster(var step: Double) : EmissionFactorScheduleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorSchedule, value: Double): Double = roundTo(value, step)
}

/** Runs the emission factor schedule steps in order and keeps a trace of each. */
data class EmissionFactorScheduleEngine(
    var steps: MutableList<EmissionFactorScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EmissionFactorSchedule): Double {
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

    fun evaluateAll(items: MutableList<EmissionFactorSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEmissionFactorScheduleEngine(): EmissionFactorScheduleEngine {
    return EmissionFactorScheduleEngine(
        steps = mutableListOf<EmissionFactorScheduleStep>(
            TieredEmissionFactorScheduleAdjuster(
                mutableListOf<Double>(961.0, 5464.0, 6724.0, 6844.0),
                mutableListOf<Double>(0.88, 1.11, 0.89, 1.12),
            ),
            TitleEmissionFactorScheduleAdjuster(
                mutableMapOf<String, Double>("south" to 0.92, "central" to 0.89),
            ),
            PercentEmissionFactorScheduleAdjuster(1.4),
            RoundingEmissionFactorScheduleAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 16310.2,
    )
}
