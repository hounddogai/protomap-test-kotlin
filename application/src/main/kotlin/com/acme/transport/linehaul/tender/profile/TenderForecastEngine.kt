package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tender forecast calculation. */
interface TenderForecastModifier {
    fun describe(): String
    fun adjust(item: TenderForecast, value: Double): Double
}

data class CategoryTenderForecastAdjuster(var factors: MutableMap<String, Double>) : TenderForecastModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: TenderForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentTenderForecastAdjuster(var percent: Double) : TenderForecastModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTenderForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TenderForecastModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TenderForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TenderForecastBatchSizeOffsetAdjuster(var weight: Double) : TenderForecastModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: TenderForecast, value: Double): Double = value + item.batchSize.toDouble() * weight
}

/** Runs the tender forecast steps in order and keeps a trace of each. */
data class TenderForecastEngine(
    var steps: MutableList<TenderForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TenderForecast): Double {
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

    fun evaluateAll(items: MutableList<TenderForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTenderForecastEngine(flags: Registry): TenderForecastEngine {
    return TenderForecastEngine(
        steps = mutableListOf<TenderForecastModifier>(
            CategoryTenderForecastAdjuster(
                mutableMapOf<String, Double>("west" to 1.24, "east" to 1.33, "central" to 0.73),
            ),
            PercentTenderForecastAdjuster(-16.4),
            FlaggedTenderForecastAdjuster(flags, "tender-forecast-weighted", -2.1),
            TenderForecastBatchSizeOffsetAdjuster(0.08),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 20163.5,
    )
}
