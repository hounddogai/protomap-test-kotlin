package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global dock door template calculation. */
interface GlobalDockDoorTemplateFactor {
    fun describe(): String
    fun adjust(item: GlobalDockDoorTemplate, value: Double): Double
}

data class GlobalDockDoorTemplateCreatedAtOffsetAdjuster(var weight: Double) : GlobalDockDoorTemplateFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: GlobalDockDoorTemplate, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class RoundingGlobalDockDoorTemplateAdjuster(var step: Double) : GlobalDockDoorTemplateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalDockDoorTemplate, value: Double): Double = roundTo(value, step)
}

data class PercentGlobalDockDoorTemplateAdjuster(var percent: Double) : GlobalDockDoorTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalDockDoorTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapGlobalDockDoorTemplateAdjuster(var cap: Double) : GlobalDockDoorTemplateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalDockDoorTemplate, value: Double): Double = minOf(value, cap)
}

data class MinimumGlobalDockDoorTemplateAdjuster(var minimum: Double) : GlobalDockDoorTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalDockDoorTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global dock door template steps in order and keeps a trace of each. */
data class GlobalDockDoorTemplateEngine(
    var steps: MutableList<GlobalDockDoorTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalDockDoorTemplate): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalDockDoorTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalDockDoorTemplateEngine(): GlobalDockDoorTemplateEngine {
    return GlobalDockDoorTemplateEngine(
        steps = mutableListOf<GlobalDockDoorTemplateFactor>(
            GlobalDockDoorTemplateCreatedAtOffsetAdjuster(1.18),
            RoundingGlobalDockDoorTemplateAdjuster(1.0),
            PercentGlobalDockDoorTemplateAdjuster(-14.3),
            CapGlobalDockDoorTemplateAdjuster(14219.0),
            MinimumGlobalDockDoorTemplateAdjuster(28.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 49216.1,
    )
}
