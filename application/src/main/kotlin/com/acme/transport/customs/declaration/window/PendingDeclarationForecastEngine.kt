package com.acme.transport.customs.declaration.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending declaration forecast calculation. */
interface PendingDeclarationForecastModifier {
    fun describe(): String
    fun adjust(item: PendingDeclarationForecast, value: Double): Double
}

data class CategoryPendingDeclarationForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingDeclarationForecastModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: PendingDeclarationForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PendingDeclarationForecastRatioOffsetAdjuster(
    var weight: Double,
) : PendingDeclarationForecastModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: PendingDeclarationForecast, value: Double): Double = value + item.ratio * weight
}

data class FlaggedPendingDeclarationForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingDeclarationForecastModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingDeclarationForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPendingDeclarationForecastAdjuster(var step: Double) : PendingDeclarationForecastModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingDeclarationForecast, value: Double): Double = roundTo(value, step)
}

/** Runs the pending declaration forecast steps in order and keeps a trace of each. */
data class PendingDeclarationForecastEngine(
    var steps: MutableList<PendingDeclarationForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingDeclarationForecast): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingDeclarationForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingDeclarationForecastEngine(flags: Registry): PendingDeclarationForecastEngine {
    return PendingDeclarationForecastEngine(
        steps = mutableListOf<PendingDeclarationForecastModifier>(
            CategoryPendingDeclarationForecastAdjuster(
                mutableMapOf<String, Double>("west" to 1.08, "east" to 1.18, "retail" to 0.8),
            ),
            PendingDeclarationForecastRatioOffsetAdjuster(0.64),
            FlaggedPendingDeclarationForecastAdjuster(flags, "pending-declaration-forecast-rolling", -8.5),
            RoundingPendingDeclarationForecastAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 96165.7,
    )
}
