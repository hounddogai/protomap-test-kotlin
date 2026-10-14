package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.flags.Registry

/** One step of the lenient coaching template calculation. */
interface LenientCoachingTemplateStep {
    fun describe(): String
    fun adjust(item: LenientCoachingTemplate, value: Double): Double
}

data class CapLenientCoachingTemplateAdjuster(var cap: Double) : LenientCoachingTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientCoachingTemplate, value: Double): Double = minOf(value, cap)
}

data class PercentLenientCoachingTemplateAdjuster(var percent: Double) : LenientCoachingTemplateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientCoachingTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLenientCoachingTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientCoachingTemplateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientCoachingTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LenientCoachingTemplateRateOffsetAdjuster(var weight: Double) : LenientCoachingTemplateStep {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: LenientCoachingTemplate, value: Double): Double = value + item.rate * weight
}

/** Runs the lenient coaching template steps in order and keeps a trace of each. */
data class LenientCoachingTemplateEngine(
    var steps: MutableList<LenientCoachingTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientCoachingTemplate): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientCoachingTemplateStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientCoachingTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientCoachingTemplateEngine(flags: Registry): LenientCoachingTemplateEngine {
    return LenientCoachingTemplateEngine(
        steps = mutableListOf<LenientCoachingTemplateStep>(
            CapLenientCoachingTemplateAdjuster(36268.0),
            PercentLenientCoachingTemplateAdjuster(-7.1),
            FlaggedLenientCoachingTemplateAdjuster(flags, "lenient-coaching-template-capped", -2.9),
            LenientCoachingTemplateRateOffsetAdjuster(1.61),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 13290.7,
    )
}
