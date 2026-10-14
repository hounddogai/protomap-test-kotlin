package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary dock door template calculation. */
interface PrimaryDockDoorTemplateFactor {
    fun describe(): String
    fun adjust(item: PrimaryDockDoorTemplate, value: Double): Double
}

data class PercentPrimaryDockDoorTemplateAdjuster(var percent: Double) : PrimaryDockDoorTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryDockDoorTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PrimaryDockDoorTemplateVersionOffsetAdjuster(var weight: Double) : PrimaryDockDoorTemplateFactor {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: PrimaryDockDoorTemplate, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class MinimumPrimaryDockDoorTemplateAdjuster(var minimum: Double) : PrimaryDockDoorTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryDockDoorTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPrimaryDockDoorTemplateAdjuster(var step: Double) : PrimaryDockDoorTemplateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryDockDoorTemplate, value: Double): Double = roundTo(value, step)
}

data class TitlePrimaryDockDoorTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryDockDoorTemplateFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: PrimaryDockDoorTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPrimaryDockDoorTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryDockDoorTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryDockDoorTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the primary dock door template steps in order and keeps a trace of each. */
data class PrimaryDockDoorTemplateEngine(
    var steps: MutableList<PrimaryDockDoorTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryDockDoorTemplate): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryDockDoorTemplateFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryDockDoorTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryDockDoorTemplateEngine(flags: Registry): PrimaryDockDoorTemplateEngine {
    return PrimaryDockDoorTemplateEngine(
        steps = mutableListOf<PrimaryDockDoorTemplateFactor>(
            PercentPrimaryDockDoorTemplateAdjuster(-12.5),
            PrimaryDockDoorTemplateVersionOffsetAdjuster(0.22),
            MinimumPrimaryDockDoorTemplateAdjuster(1.8),
            RoundingPrimaryDockDoorTemplateAdjuster(1.0),
            TitlePrimaryDockDoorTemplateAdjuster(
                mutableMapOf<String, Double>("retail" to 0.85, "online" to 1.13, "west" to 1.36),
            ),
            FlaggedPrimaryDockDoorTemplateAdjuster(flags, "primary-dock-door-template-pending", -2.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 41216.1,
    )
}
