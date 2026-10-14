package com.acme.transport.customs.inspection.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global inspection estimate calculation. */
interface GlobalInspectionEstimateFactor {
    fun describe(): String
    fun adjust(item: GlobalInspectionEstimate, value: Double): Double
}

data class PercentGlobalInspectionEstimateAdjuster(var percent: Double) : GlobalInspectionEstimateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalInspectionEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumGlobalInspectionEstimateAdjuster(var minimum: Double) : GlobalInspectionEstimateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalInspectionEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapGlobalInspectionEstimateAdjuster(var cap: Double) : GlobalInspectionEstimateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalInspectionEstimate, value: Double): Double = minOf(value, cap)
}

data class FlaggedGlobalInspectionEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalInspectionEstimateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalInspectionEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalInspectionEstimateAdjuster(var step: Double) : GlobalInspectionEstimateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalInspectionEstimate, value: Double): Double = roundTo(value, step)
}

/** Runs the global inspection estimate steps in order and keeps a trace of each. */
data class GlobalInspectionEstimateEngine(
    var steps: MutableList<GlobalInspectionEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalInspectionEstimate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalInspectionEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalInspectionEstimateEngine(flags: Registry): GlobalInspectionEstimateEngine {
    return GlobalInspectionEstimateEngine(
        steps = mutableListOf<GlobalInspectionEstimateFactor>(
            PercentGlobalInspectionEstimateAdjuster(19.8),
            MinimumGlobalInspectionEstimateAdjuster(37.0),
            CapGlobalInspectionEstimateAdjuster(38093.0),
            FlaggedGlobalInspectionEstimateAdjuster(flags, "global-inspection-estimate-capped", 4.3),
            RoundingGlobalInspectionEstimateAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 6302.4,
    )
}
