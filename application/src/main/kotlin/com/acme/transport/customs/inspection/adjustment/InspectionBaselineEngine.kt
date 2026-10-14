package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection baseline calculation. */
interface InspectionBaselineFactor {
    fun describe(): String
    fun adjust(item: InspectionBaseline, value: Double): Double
}

data class MinimumInspectionBaselineAdjuster(var minimum: Double) : InspectionBaselineFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class InspectionBaselineQuantityOffsetAdjuster(var weight: Double) : InspectionBaselineFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: InspectionBaseline, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class LabelInspectionBaselineAdjuster(var factors: MutableMap<String, Double>) : InspectionBaselineFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: InspectionBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedInspectionBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionBaselineFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapInspectionBaselineAdjuster(var cap: Double) : InspectionBaselineFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionBaseline, value: Double): Double = minOf(value, cap)
}

data class RoundingInspectionBaselineAdjuster(var step: Double) : InspectionBaselineFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionBaseline, value: Double): Double = roundTo(value, step)
}

/** Runs the inspection baseline steps in order and keeps a trace of each. */
data class InspectionBaselineEngine(
    var steps: MutableList<InspectionBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionBaseline): Double {
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

    fun evaluateAll(items: MutableList<InspectionBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionBaselineEngine(flags: Registry): InspectionBaselineEngine {
    return InspectionBaselineEngine(
        steps = mutableListOf<InspectionBaselineFactor>(
            MinimumInspectionBaselineAdjuster(19.1),
            InspectionBaselineQuantityOffsetAdjuster(0.64),
            LabelInspectionBaselineAdjuster(mutableMapOf<String, Double>("central" to 0.8)),
            FlaggedInspectionBaselineAdjuster(flags, "inspection-baseline-regional", -7.9),
            CapInspectionBaselineAdjuster(42095.0),
            RoundingInspectionBaselineAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 48627.3,
    )
}
