package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.kit.flags.Registry

/** One step of the regional carbon report rule calculation. */
interface RegionalCarbonReportRuleModifier {
    fun describe(): String
    fun adjust(item: RegionalCarbonReportRule, value: Double): Double
}

data class PercentRegionalCarbonReportRuleAdjuster(var percent: Double) : RegionalCarbonReportRuleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalCarbonReportRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRegionalCarbonReportRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalCarbonReportRuleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalCarbonReportRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class UnitRegionalCarbonReportRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCarbonReportRuleModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: RegionalCarbonReportRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalCarbonReportRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalCarbonReportRuleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalCarbonReportRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalCarbonReportRuleAdjuster(var cap: Double) : RegionalCarbonReportRuleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalCarbonReportRule, value: Double): Double = minOf(value, cap)
}

/** Runs the regional carbon report rule steps in order and keeps a trace of each. */
data class RegionalCarbonReportRuleEngine(
    var steps: MutableList<RegionalCarbonReportRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalCarbonReportRule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalCarbonReportRuleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalCarbonReportRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalCarbonReportRuleEngine(flags: Registry): RegionalCarbonReportRuleEngine {
    return RegionalCarbonReportRuleEngine(
        steps = mutableListOf<RegionalCarbonReportRuleModifier>(
            PercentRegionalCarbonReportRuleAdjuster(12.0),
            TieredRegionalCarbonReportRuleAdjuster(
                mutableListOf<Double>(1457.0, 2921.0, 3157.0, 8512.0),
                mutableListOf<Double>(1.15, 1.07, 1.07, 0.9),
            ),
            UnitRegionalCarbonReportRuleAdjuster(mutableMapOf<String, Double>("east" to 1.31)),
            FlaggedRegionalCarbonReportRuleAdjuster(flags, "regional-carbon-report-rule-capped", 9.7),
            CapRegionalCarbonReportRuleAdjuster(40554.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 94205.9,
    )
}
