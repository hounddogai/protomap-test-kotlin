package com.acme.transport.customs.inspection.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the inspection adjustment calculation. */
interface InspectionAdjustmentModifier {
    fun describe(): String
    fun adjust(item: InspectionAdjustment, value: Double): Double
}

data class CapInspectionAdjustmentAdjuster(var cap: Double) : InspectionAdjustmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionAdjustment, value: Double): Double = minOf(value, cap)
}

data class MinimumInspectionAdjustmentAdjuster(var minimum: Double) : InspectionAdjustmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentInspectionAdjustmentAdjuster(var percent: Double) : InspectionAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedInspectionAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionAdjustmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryInspectionAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : InspectionAdjustmentModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: InspectionAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the inspection adjustment steps in order and keeps a trace of each. */
data class InspectionAdjustmentEngine(
    var steps: MutableList<InspectionAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: InspectionAdjustment): Double {
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

    fun evaluateAll(items: MutableList<InspectionAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultInspectionAdjustmentEngine(flags: Registry): InspectionAdjustmentEngine {
    return InspectionAdjustmentEngine(
        steps = mutableListOf<InspectionAdjustmentModifier>(
            CapInspectionAdjustmentAdjuster(33178.0),
            MinimumInspectionAdjustmentAdjuster(13.3),
            PercentInspectionAdjustmentAdjuster(-23.8),
            FlaggedInspectionAdjustmentAdjuster(flags, "inspection-adjustment-global", -5.1),
            CategoryInspectionAdjustmentAdjuster(mutableMapOf<String, Double>("east" to 1.34, "central" to 1.1)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 16450.1,
    )
}
