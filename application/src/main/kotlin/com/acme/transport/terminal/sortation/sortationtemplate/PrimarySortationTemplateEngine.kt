package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.flags.Registry

/** One step of the primary sortation template calculation. */
interface PrimarySortationTemplateFactor {
    fun describe(): String
    fun adjust(item: PrimarySortationTemplate, value: Double): Double
}

data class PrimarySortationTemplateMarginOffsetAdjuster(var weight: Double) : PrimarySortationTemplateFactor {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: PrimarySortationTemplate, value: Double): Double = value + item.margin * weight
}

data class FlaggedPrimarySortationTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimarySortationTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimarySortationTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPrimarySortationTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimarySortationTemplateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimarySortationTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPrimarySortationTemplateAdjuster(var percent: Double) : PrimarySortationTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimarySortationTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPrimarySortationTemplateAdjuster(var cap: Double) : PrimarySortationTemplateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimarySortationTemplate, value: Double): Double = minOf(value, cap)
}

/** Runs the primary sortation template steps in order and keeps a trace of each. */
data class PrimarySortationTemplateEngine(
    var steps: MutableList<PrimarySortationTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimarySortationTemplate): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimarySortationTemplateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PrimarySortationTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimarySortationTemplateEngine(flags: Registry): PrimarySortationTemplateEngine {
    return PrimarySortationTemplateEngine(
        steps = mutableListOf<PrimarySortationTemplateFactor>(
            PrimarySortationTemplateMarginOffsetAdjuster(1.52),
            FlaggedPrimarySortationTemplateAdjuster(flags, "primary-sortation-template-scheduled", 5.8),
            TieredPrimarySortationTemplateAdjuster(
                mutableListOf<Double>(465.0, 576.0, 1767.0, 8458.0),
                mutableListOf<Double>(1.01, 1.12, 1.08, 1.04),
            ),
            PercentPrimarySortationTemplateAdjuster(13.5),
            CapPrimarySortationTemplateAdjuster(37961.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 62867.7,
    )
}
