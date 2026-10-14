package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal tariff rule calculation. */
interface SeasonalTariffRuleAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalTariffRule, value: Double): Double
}

data class PercentSeasonalTariffRuleAdjuster(var percent: Double) : SeasonalTariffRuleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalTariffRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SeasonalTariffRuleScoreOffsetAdjuster(var weight: Double) : SeasonalTariffRuleAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: SeasonalTariffRule, value: Double): Double = value + item.score * weight
}

data class CategorySeasonalTariffRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalTariffRuleAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: SeasonalTariffRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal tariff rule steps in order and keeps a trace of each. */
data class SeasonalTariffRuleEngine(
    var steps: MutableList<SeasonalTariffRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalTariffRule): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalTariffRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalTariffRuleEngine(): SeasonalTariffRuleEngine {
    return SeasonalTariffRuleEngine(
        steps = mutableListOf<SeasonalTariffRuleAdjuster>(
            PercentSeasonalTariffRuleAdjuster(-23.8),
            SeasonalTariffRuleScoreOffsetAdjuster(0.34),
            CategorySeasonalTariffRuleAdjuster(mutableMapOf<String, Double>("west" to 1.26, "retail" to 1.03)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 98908.5,
    )
}
