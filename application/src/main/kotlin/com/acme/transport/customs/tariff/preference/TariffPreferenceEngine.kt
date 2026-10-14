package com.acme.transport.customs.tariff.preference

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tariff preference calculation. */
interface TariffPreferenceStep {
    fun describe(): String
    fun adjust(item: TariffPreference, value: Double): Double
}

data class PercentTariffPreferenceAdjuster(var percent: Double) : TariffPreferenceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TariffPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingTariffPreferenceAdjuster(var step: Double) : TariffPreferenceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffPreference, value: Double): Double = roundTo(value, step)
}

data class MinimumTariffPreferenceAdjuster(var minimum: Double) : TariffPreferenceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TariffPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTariffPreferenceAdjuster(var cap: Double) : TariffPreferenceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TariffPreference, value: Double): Double = minOf(value, cap)
}

/** Runs the tariff preference steps in order and keeps a trace of each. */
data class TariffPreferenceEngine(
    var steps: MutableList<TariffPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TariffPreference): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TariffPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTariffPreferenceEngine(): TariffPreferenceEngine {
    return TariffPreferenceEngine(
        steps = mutableListOf<TariffPreferenceStep>(
            PercentTariffPreferenceAdjuster(8.0),
            RoundingTariffPreferenceAdjuster(0.5),
            MinimumTariffPreferenceAdjuster(25.3),
            CapTariffPreferenceAdjuster(21328.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 4817.6,
    )
}
