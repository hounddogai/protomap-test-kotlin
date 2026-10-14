package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the defect template calculation. */
interface DefectTemplateStep {
    fun describe(): String
    fun adjust(item: DefectTemplate, value: Double): Double
}

data class RoundingDefectTemplateAdjuster(var step: Double) : DefectTemplateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefectTemplate, value: Double): Double = roundTo(value, step)
}

data class MinimumDefectTemplateAdjuster(var minimum: Double) : DefectTemplateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDefectTemplateAdjuster(var cap: Double) : DefectTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectTemplate, value: Double): Double = minOf(value, cap)
}

data class NameDefectTemplateAdjuster(var factors: MutableMap<String, Double>) : DefectTemplateStep {
    override fun describe(): String = "by name"

    override fun adjust(item: DefectTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the defect template steps in order and keeps a trace of each. */
data class DefectTemplateEngine(
    var steps: MutableList<DefectTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefectTemplate): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefectTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefectTemplateEngine(): DefectTemplateEngine {
    return DefectTemplateEngine(
        steps = mutableListOf<DefectTemplateStep>(
            RoundingDefectTemplateAdjuster(0.25),
            MinimumDefectTemplateAdjuster(49.1),
            CapDefectTemplateAdjuster(16131.0),
            NameDefectTemplateAdjuster(
                mutableMapOf<String, Double>("central" to 0.94, "south" to 1.02, "north" to 1.07),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 58225.1,
    )
}
