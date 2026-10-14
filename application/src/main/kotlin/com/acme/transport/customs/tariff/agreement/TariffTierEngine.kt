package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.numbers.roundTo

/** One step of the tariff tier calculation. */
interface TariffTierFactor {
    fun describe(): String
    fun adjust(item: TariffTier, value: Double): Double
}

data class RoundingTariffTierAdjuster(var step: Double) : TariffTierFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffTier, value: Double): Double = roundTo(value, step)
}

data class TariffTierStopCountOffsetAdjuster(var weight: Double) : TariffTierFactor {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: TariffTier, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class CapTariffTierAdjuster(var cap: Double) : TariffTierFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TariffTier, value: Double): Double = minOf(value, cap)
}

/** Runs the tariff tier steps in order and keeps a trace of each. */
data class TariffTierEngine(
    var steps: MutableList<TariffTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TariffTier): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TariffTierFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TariffTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTariffTierEngine(): TariffTierEngine {
    return TariffTierEngine(
        steps = mutableListOf<TariffTierFactor>(
            RoundingTariffTierAdjuster(5.0),
            TariffTierStopCountOffsetAdjuster(1.13),
            CapTariffTierAdjuster(43047.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 80842.9,
    )
}
