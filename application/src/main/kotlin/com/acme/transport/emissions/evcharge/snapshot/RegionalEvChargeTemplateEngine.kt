package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.kit.flags.Registry

/** One step of the regional ev charge template calculation. */
interface RegionalEvChargeTemplateModifier {
    fun describe(): String
    fun adjust(item: RegionalEvChargeTemplate, value: Double): Double
}

data class FlaggedRegionalEvChargeTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalEvChargeTemplateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalEvChargeTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRegionalEvChargeTemplateAdjuster(var percent: Double) : RegionalEvChargeTemplateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalEvChargeTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionalEvChargeTemplateRateOffsetAdjuster(var weight: Double) : RegionalEvChargeTemplateModifier {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: RegionalEvChargeTemplate, value: Double): Double = value + item.rate * weight
}

data class CapRegionalEvChargeTemplateAdjuster(var cap: Double) : RegionalEvChargeTemplateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalEvChargeTemplate, value: Double): Double = minOf(value, cap)
}

/** Runs the regional ev charge template steps in order and keeps a trace of each. */
data class RegionalEvChargeTemplateEngine(
    var steps: MutableList<RegionalEvChargeTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalEvChargeTemplate): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalEvChargeTemplateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalEvChargeTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalEvChargeTemplateEngine(flags: Registry): RegionalEvChargeTemplateEngine {
    return RegionalEvChargeTemplateEngine(
        steps = mutableListOf<RegionalEvChargeTemplateModifier>(
            FlaggedRegionalEvChargeTemplateAdjuster(flags, "regional-ev-charge-template-capped", 2.3),
            PercentRegionalEvChargeTemplateAdjuster(-1.2),
            RegionalEvChargeTemplateRateOffsetAdjuster(0.55),
            CapRegionalEvChargeTemplateAdjuster(4720.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 76358.9,
    )
}
