package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default carbon report mapping calculation. */
interface DefaultCarbonReportMappingModifier {
    fun describe(): String
    fun adjust(item: DefaultCarbonReportMapping, value: Double): Double
}

data class RoundingDefaultCarbonReportMappingAdjuster(var step: Double) : DefaultCarbonReportMappingModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultCarbonReportMapping, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultCarbonReportMappingAdjuster(var minimum: Double) : DefaultCarbonReportMappingModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultCarbonReportMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DefaultCarbonReportMappingStartsAtOffsetAdjuster(
    var weight: Double,
) : DefaultCarbonReportMappingModifier {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: DefaultCarbonReportMapping, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

/** Runs the default carbon report mapping steps in order and keeps a trace of each. */
data class DefaultCarbonReportMappingEngine(
    var steps: MutableList<DefaultCarbonReportMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultCarbonReportMapping): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultCarbonReportMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultCarbonReportMappingEngine(): DefaultCarbonReportMappingEngine {
    return DefaultCarbonReportMappingEngine(
        steps = mutableListOf<DefaultCarbonReportMappingModifier>(
            RoundingDefaultCarbonReportMappingAdjuster(5.0),
            MinimumDefaultCarbonReportMappingAdjuster(16.0),
            DefaultCarbonReportMappingStartsAtOffsetAdjuster(1.99),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 15033.8,
    )
}
