package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered tariff template calculation. */
interface TieredTariffTemplateModifier {
    fun describe(): String
    fun adjust(item: TieredTariffTemplate, value: Double): Double
}

data class RoundingTieredTariffTemplateAdjuster(var step: Double) : TieredTariffTemplateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredTariffTemplate, value: Double): Double = roundTo(value, step)
}

data class PercentTieredTariffTemplateAdjuster(var percent: Double) : TieredTariffTemplateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredTariffTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTieredTariffTemplateAdjuster(var cap: Double) : TieredTariffTemplateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredTariffTemplate, value: Double): Double = minOf(value, cap)
}

data class TitleTieredTariffTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredTariffTemplateModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: TieredTariffTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered tariff template steps in order and keeps a trace of each. */
data class TieredTariffTemplateEngine(
    var steps: MutableList<TieredTariffTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredTariffTemplate): Double {
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

    fun evaluateAll(items: MutableList<TieredTariffTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredTariffTemplateEngine(): TieredTariffTemplateEngine {
    return TieredTariffTemplateEngine(
        steps = mutableListOf<TieredTariffTemplateModifier>(
            RoundingTieredTariffTemplateAdjuster(5.0),
            PercentTieredTariffTemplateAdjuster(19.8),
            CapTieredTariffTemplateAdjuster(12342.0),
            TitleTieredTariffTemplateAdjuster(
                mutableMapOf<String, Double>("retail" to 1.25, "north" to 1.34, "east" to 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 32433.6,
    )
}
