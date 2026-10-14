package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.flags.Registry

/** One step of the dwell template calculation. */
interface DwellTemplateAdjuster {
    fun describe(): String
    fun adjust(item: DwellTemplate, value: Double): Double
}

data class PercentDwellTemplateAdjuster(var percent: Double) : DwellTemplateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CategoryDwellTemplateAdjuster(var factors: MutableMap<String, Double>) : DwellTemplateAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: DwellTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDwellTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DwellTemplateQuantityOffsetAdjuster(var weight: Double) : DwellTemplateAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: DwellTemplate, value: Double): Double = value + item.quantity.toDouble() * weight
}

/** Runs the dwell template steps in order and keeps a trace of each. */
data class DwellTemplateEngine(
    var steps: MutableList<DwellTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DwellTemplate): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DwellTemplateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DwellTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDwellTemplateEngine(flags: Registry): DwellTemplateEngine {
    return DwellTemplateEngine(
        steps = mutableListOf<DwellTemplateAdjuster>(
            PercentDwellTemplateAdjuster(16.9),
            CategoryDwellTemplateAdjuster(mutableMapOf<String, Double>("west" to 1.3, "north" to 0.85)),
            FlaggedDwellTemplateAdjuster(flags, "dwell-template-weighted", 9.2),
            DwellTemplateQuantityOffsetAdjuster(1.57),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 40085.6,
    )
}
