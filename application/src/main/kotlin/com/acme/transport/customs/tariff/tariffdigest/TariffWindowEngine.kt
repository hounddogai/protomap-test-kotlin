package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tariff window calculation. */
interface TariffWindowStep {
    fun describe(): String
    fun adjust(item: TariffWindow, value: Double): Double
}

data class FlaggedTariffWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TariffWindowStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TariffWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTariffWindowAdjuster(var step: Double) : TariffWindowStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffWindow, value: Double): Double = roundTo(value, step)
}

data class TariffWindowEtaMinutesOffsetAdjuster(var weight: Double) : TariffWindowStep {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: TariffWindow, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class PercentTariffWindowAdjuster(var percent: Double) : TariffWindowStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TariffWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tariff window steps in order and keeps a trace of each. */
data class TariffWindowEngine(
    var steps: MutableList<TariffWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TariffWindow): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TariffWindowStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TariffWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTariffWindowEngine(flags: Registry): TariffWindowEngine {
    return TariffWindowEngine(
        steps = mutableListOf<TariffWindowStep>(
            FlaggedTariffWindowAdjuster(flags, "tariff-window-manual", -1.4),
            RoundingTariffWindowAdjuster(1.0),
            TariffWindowEtaMinutesOffsetAdjuster(1.79),
            PercentTariffWindowAdjuster(-19.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 21626.1,
    )
}
