package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional tender template calculation. */
interface RegionalTenderTemplateAdjuster {
    fun describe(): String
    fun adjust(item: RegionalTenderTemplate, value: Double): Double
}

data class PercentRegionalTenderTemplateAdjuster(var percent: Double) : RegionalTenderTemplateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalTenderTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRegionalTenderTemplateAdjuster(var step: Double) : RegionalTenderTemplateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalTenderTemplate, value: Double): Double = roundTo(value, step)
}

data class FlaggedRegionalTenderTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalTenderTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalTenderTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional tender template steps in order and keeps a trace of each. */
data class RegionalTenderTemplateEngine(
    var steps: MutableList<RegionalTenderTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalTenderTemplate): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalTenderTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalTenderTemplateEngine(flags: Registry): RegionalTenderTemplateEngine {
    return RegionalTenderTemplateEngine(
        steps = mutableListOf<RegionalTenderTemplateAdjuster>(
            PercentRegionalTenderTemplateAdjuster(23.8),
            RoundingRegionalTenderTemplateAdjuster(0.1),
            FlaggedRegionalTenderTemplateAdjuster(flags, "regional-tender-template-adaptive", 0.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 57275.6,
    )
}
