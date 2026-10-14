package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.numbers.roundTo

/** One step of the tariff checkpoint calculation. */
interface TariffCheckpointStep {
    fun describe(): String
    fun adjust(item: TariffCheckpoint, value: Double): Double
}

data class PercentTariffCheckpointAdjuster(var percent: Double) : TariffCheckpointStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TariffCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryTariffCheckpointAdjuster(var factors: MutableMap<String, Double>) : TariffCheckpointStep {
    override fun describe(): String = "by country"

    override fun adjust(item: TariffCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTariffCheckpointAdjuster(var step: Double) : TariffCheckpointStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffCheckpoint, value: Double): Double = roundTo(value, step)
}

data class TieredTariffCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TariffCheckpointStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TariffCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tariff checkpoint steps in order and keeps a trace of each. */
data class TariffCheckpointEngine(
    var steps: MutableList<TariffCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TariffCheckpoint): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TariffCheckpointStep = steps[index]
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

    fun evaluateAll(items: MutableList<TariffCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTariffCheckpointEngine(): TariffCheckpointEngine {
    return TariffCheckpointEngine(
        steps = mutableListOf<TariffCheckpointStep>(
            PercentTariffCheckpointAdjuster(6.3),
            CountryTariffCheckpointAdjuster(
                mutableMapOf<String, Double>("central" to 1.11, "retail" to 0.84, "west" to 0.74),
            ),
            RoundingTariffCheckpointAdjuster(1.0),
            TieredTariffCheckpointAdjuster(
                mutableListOf<Double>(5272.0, 9547.0),
                mutableListOf<Double>(0.81, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 78571.5,
    )
}
