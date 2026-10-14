package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the dock door template calculation. */
interface DockDoorTemplateModifier {
    fun describe(): String
    fun adjust(item: DockDoorTemplate, value: Double): Double
}

data class RoundingDockDoorTemplateAdjuster(var step: Double) : DockDoorTemplateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorTemplate, value: Double): Double = roundTo(value, step)
}

data class FlaggedDockDoorTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorTemplateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDockDoorTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorTemplateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CountryDockDoorTemplateAdjuster(var factors: MutableMap<String, Double>) : DockDoorTemplateModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: DockDoorTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class DockDoorTemplateMaximumOffsetAdjuster(var weight: Double) : DockDoorTemplateModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: DockDoorTemplate, value: Double): Double = value + item.maximum * weight
}

data class PercentDockDoorTemplateAdjuster(var percent: Double) : DockDoorTemplateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the dock door template steps in order and keeps a trace of each. */
data class DockDoorTemplateEngine(
    var steps: MutableList<DockDoorTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DockDoorTemplate): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorTemplateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDockDoorTemplateEngine(flags: Registry): DockDoorTemplateEngine {
    return DockDoorTemplateEngine(
        steps = mutableListOf<DockDoorTemplateModifier>(
            RoundingDockDoorTemplateAdjuster(0.05),
            FlaggedDockDoorTemplateAdjuster(flags, "dock-door-template-legacy", 5.3),
            TieredDockDoorTemplateAdjuster(
                mutableListOf<Double>(2193.0, 3804.0),
                mutableListOf<Double>(0.9, 0.92),
            ),
            CountryDockDoorTemplateAdjuster(mutableMapOf<String, Double>("central" to 0.87)),
            DockDoorTemplateMaximumOffsetAdjuster(1.92),
            PercentDockDoorTemplateAdjuster(-10.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 97045.4,
    )
}
