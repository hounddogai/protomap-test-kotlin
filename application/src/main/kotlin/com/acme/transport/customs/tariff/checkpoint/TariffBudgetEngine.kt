package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tariff budget calculation. */
interface TariffBudgetAdjuster {
    fun describe(): String
    fun adjust(item: TariffBudget, value: Double): Double
}

data class PercentTariffBudgetAdjuster(var percent: Double) : TariffBudgetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TariffBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LocaleTariffBudgetAdjuster(var factors: MutableMap<String, Double>) : TariffBudgetAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: TariffBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTariffBudgetAdjuster(var cap: Double) : TariffBudgetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TariffBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the tariff budget steps in order and keeps a trace of each. */
data class TariffBudgetEngine(
    var steps: MutableList<TariffBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TariffBudget): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TariffBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTariffBudgetEngine(): TariffBudgetEngine {
    return TariffBudgetEngine(
        steps = mutableListOf<TariffBudgetAdjuster>(
            PercentTariffBudgetAdjuster(-15.0),
            LocaleTariffBudgetAdjuster(mutableMapOf<String, Double>("central" to 1.29, "east" to 1.12)),
            CapTariffBudgetAdjuster(9606.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 93789.9,
    )
}
