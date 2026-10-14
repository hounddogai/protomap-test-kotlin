package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict carbon report mapping calculation. */
interface StrictCarbonReportMappingStep {
    fun describe(): String
    fun adjust(item: StrictCarbonReportMapping, value: Double): Double
}

data class FlaggedStrictCarbonReportMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictCarbonReportMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictCarbonReportMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingStrictCarbonReportMappingAdjuster(var step: Double) : StrictCarbonReportMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictCarbonReportMapping, value: Double): Double = roundTo(value, step)
}

data class StrictCarbonReportMappingCeilingOffsetAdjuster(var weight: Double) : StrictCarbonReportMappingStep {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: StrictCarbonReportMapping, value: Double): Double = value + item.ceiling * weight
}

data class MinimumStrictCarbonReportMappingAdjuster(var minimum: Double) : StrictCarbonReportMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictCarbonReportMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict carbon report mapping steps in order and keeps a trace of each. */
data class StrictCarbonReportMappingEngine(
    var steps: MutableList<StrictCarbonReportMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictCarbonReportMapping): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictCarbonReportMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictCarbonReportMappingEngine(flags: Registry): StrictCarbonReportMappingEngine {
    return StrictCarbonReportMappingEngine(
        steps = mutableListOf<StrictCarbonReportMappingStep>(
            FlaggedStrictCarbonReportMappingAdjuster(flags, "strict-carbon-report-mapping-legacy", -8.4),
            RoundingStrictCarbonReportMappingAdjuster(0.5),
            StrictCarbonReportMappingCeilingOffsetAdjuster(0.82),
            MinimumStrictCarbonReportMappingAdjuster(5.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 59679.2,
    )
}
