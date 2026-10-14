package com.acme.transport.customs.inspection.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection estimate calculation. */
interface InspectionEstimateFactor {
    fun describe(): String
    fun adjust(item: InspectionEstimate, value: Double): Double
}

data class RoundingInspectionEstimateAdjuster(var step: Double) : InspectionEstimateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionEstimate, value: Double): Double = roundTo(value, step)
}

data class CapInspectionEstimateAdjuster(var cap: Double) : InspectionEstimateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionEstimate, value: Double): Double = minOf(value, cap)
}

data class TieredInspectionEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionEstimateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedInspectionEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionEstimateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumInspectionEstimateAdjuster(var minimum: Double) : InspectionEstimateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the inspection estimate steps in order and keeps a trace of each. */
data class InspectionEstimateEngine(
    var steps: MutableList<InspectionEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionEstimate): Double {
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

    fun evaluateAll(items: MutableList<InspectionEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionEstimateEngine(flags: Registry): InspectionEstimateEngine {
    return InspectionEstimateEngine(
        steps = mutableListOf<InspectionEstimateFactor>(
            RoundingInspectionEstimateAdjuster(0.05),
            CapInspectionEstimateAdjuster(26206.0),
            TieredInspectionEstimateAdjuster(
                mutableListOf<Double>(806.0, 2658.0, 3573.0),
                mutableListOf<Double>(0.95, 0.85, 0.97),
            ),
            FlaggedInspectionEstimateAdjuster(flags, "inspection-estimate-manual", 7.7),
            MinimumInspectionEstimateAdjuster(11.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 69231.9,
    )
}
