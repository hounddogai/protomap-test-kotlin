package com.acme.transport.customs.tariff.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tariff template calculation. */
interface TariffTemplateAdjuster {
    fun describe(): String
    fun adjust(item: TariffTemplate, value: Double): Double
}

data class PercentTariffTemplateAdjuster(var percent: Double) : TariffTemplateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TariffTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTariffTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TariffTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TariffTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTariffTemplateAdjuster(var step: Double) : TariffTemplateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffTemplate, value: Double): Double = roundTo(value, step)
}

/** Runs the tariff template steps in order and keeps a trace of each. */
data class TariffTemplateEngine(
    var steps: MutableList<TariffTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TariffTemplate): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TariffTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTariffTemplateEngine(flags: Registry): TariffTemplateEngine {
    return TariffTemplateEngine(
        steps = mutableListOf<TariffTemplateAdjuster>(
            PercentTariffTemplateAdjuster(4.7),
            FlaggedTariffTemplateAdjuster(flags, "tariff-template-batched", 7.3),
            RoundingTariffTemplateAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 11534.6,
    )
}
