package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default inspection estimate calculation. */
interface DefaultInspectionEstimateStep {
    fun describe(): String
    fun adjust(item: DefaultInspectionEstimate, value: Double): Double
}

data class RoundingDefaultInspectionEstimateAdjuster(var step: Double) : DefaultInspectionEstimateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultInspectionEstimate, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultInspectionEstimateAdjuster(var minimum: Double) : DefaultInspectionEstimateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultInspectionEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DefaultInspectionEstimateStartsAtOffsetAdjuster(var weight: Double) : DefaultInspectionEstimateStep {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: DefaultInspectionEstimate, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class FlaggedDefaultInspectionEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultInspectionEstimateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultInspectionEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default inspection estimate steps in order and keeps a trace of each. */
data class DefaultInspectionEstimateEngine(
    var steps: MutableList<DefaultInspectionEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultInspectionEstimate): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultInspectionEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultInspectionEstimateEngine(flags: Registry): DefaultInspectionEstimateEngine {
    return DefaultInspectionEstimateEngine(
        steps = mutableListOf<DefaultInspectionEstimateStep>(
            RoundingDefaultInspectionEstimateAdjuster(0.5),
            MinimumDefaultInspectionEstimateAdjuster(41.2),
            DefaultInspectionEstimateStartsAtOffsetAdjuster(1.23),
            FlaggedDefaultInspectionEstimateAdjuster(flags, "default-inspection-estimate-strict", -4.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 37436.6,
    )
}
