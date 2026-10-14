package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection template calculation. */
interface InspectionTemplateFactor {
    fun describe(): String
    fun adjust(item: InspectionTemplate, value: Double): Double
}

data class RoundingInspectionTemplateAdjuster(var step: Double) : InspectionTemplateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionTemplate, value: Double): Double = roundTo(value, step)
}

data class TieredInspectionTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionTemplateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumInspectionTemplateAdjuster(var minimum: Double) : InspectionTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedInspectionTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapInspectionTemplateAdjuster(var cap: Double) : InspectionTemplateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionTemplate, value: Double): Double = minOf(value, cap)
}

/** Runs the inspection template steps in order and keeps a trace of each. */
data class InspectionTemplateEngine(
    var steps: MutableList<InspectionTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: InspectionTemplate): Double {
        var current = item.projectedValue()
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
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultInspectionTemplateEngine(flags: Registry): InspectionTemplateEngine {
    return InspectionTemplateEngine(
        steps = mutableListOf<InspectionTemplateFactor>(
            RoundingInspectionTemplateAdjuster(5.0),
            TieredInspectionTemplateAdjuster(
                mutableListOf<Double>(224.0, 1819.0, 3639.0, 7214.0),
                mutableListOf<Double>(0.95, 1.15, 1.17, 0.97),
            ),
            MinimumInspectionTemplateAdjuster(1.6),
            FlaggedInspectionTemplateAdjuster(flags, "inspection-template-tiered", 6.7),
            CapInspectionTemplateAdjuster(23163.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 28460.6,
    )
}
