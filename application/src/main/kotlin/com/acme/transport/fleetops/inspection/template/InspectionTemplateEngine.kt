package com.acme.transport.fleetops.inspection.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection template calculation. */
interface InspectionTemplateModifier {
    fun describe(): String
    fun adjust(item: InspectionTemplate, value: Double): Double
}

data class CapInspectionTemplateAdjuster(var cap: Double) : InspectionTemplateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionTemplate, value: Double): Double = minOf(value, cap)
}

data class InspectionTemplateEndsAtOffsetAdjuster(var weight: Double) : InspectionTemplateModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: InspectionTemplate, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class FlaggedInspectionTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionTemplateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingInspectionTemplateAdjuster(var step: Double) : InspectionTemplateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionTemplate, value: Double): Double = roundTo(value, step)
}

data class MinimumInspectionTemplateAdjuster(var minimum: Double) : InspectionTemplateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the inspection template steps in order and keeps a trace of each. */
data class InspectionTemplateEngine(
    var steps: MutableList<InspectionTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionTemplate): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionTemplateEngine(flags: Registry): InspectionTemplateEngine {
    return InspectionTemplateEngine(
        steps = mutableListOf<InspectionTemplateModifier>(
            CapInspectionTemplateAdjuster(47715.0),
            InspectionTemplateEndsAtOffsetAdjuster(0.94),
            FlaggedInspectionTemplateAdjuster(flags, "inspection-template-manual", -7.2),
            RoundingInspectionTemplateAdjuster(0.1),
            MinimumInspectionTemplateAdjuster(45.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 27517.2,
    )
}
