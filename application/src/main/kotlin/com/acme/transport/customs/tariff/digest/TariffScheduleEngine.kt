package com.acme.transport.customs.tariff.digest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tariff schedule calculation. */
interface TariffScheduleModifier {
    fun describe(): String
    fun adjust(item: TariffSchedule, value: Double): Double
}

data class TieredTariffScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TariffScheduleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TariffSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTariffScheduleAdjuster(var step: Double) : TariffScheduleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffSchedule, value: Double): Double = roundTo(value, step)
}

data class CountryTariffScheduleAdjuster(var factors: MutableMap<String, Double>) : TariffScheduleModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: TariffSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TariffScheduleSequenceOffsetAdjuster(var weight: Double) : TariffScheduleModifier {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: TariffSchedule, value: Double): Double = value + item.sequence.toDouble() * weight
}

/** Runs the tariff schedule steps in order and keeps a trace of each. */
data class TariffScheduleEngine(
    var steps: MutableList<TariffScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TariffSchedule): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TariffSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTariffScheduleEngine(): TariffScheduleEngine {
    return TariffScheduleEngine(
        steps = mutableListOf<TariffScheduleModifier>(
            TieredTariffScheduleAdjuster(
                mutableListOf<Double>(4678.0, 7706.0, 9120.0),
                mutableListOf<Double>(1.2, 1.03, 0.88),
            ),
            RoundingTariffScheduleAdjuster(1.0),
            CountryTariffScheduleAdjuster(mutableMapOf<String, Double>("online" to 1.05)),
            TariffScheduleSequenceOffsetAdjuster(0.62),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 43697.2,
    )
}
