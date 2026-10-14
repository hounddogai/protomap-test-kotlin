package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual carbon report rule calculation. */
interface ManualCarbonReportRuleModifier {
    fun describe(): String
    fun adjust(item: ManualCarbonReportRule, value: Double): Double
}

data class TieredManualCarbonReportRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualCarbonReportRuleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualCarbonReportRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedManualCarbonReportRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualCarbonReportRuleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualCarbonReportRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentManualCarbonReportRuleAdjuster(var percent: Double) : ManualCarbonReportRuleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualCarbonReportRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualCarbonReportRuleAdjuster(var step: Double) : ManualCarbonReportRuleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualCarbonReportRule, value: Double): Double = roundTo(value, step)
}

data class ManualCarbonReportRuleMaximumOffsetAdjuster(var weight: Double) : ManualCarbonReportRuleModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: ManualCarbonReportRule, value: Double): Double = value + item.maximum * weight
}

/** Runs the manual carbon report rule steps in order and keeps a trace of each. */
data class ManualCarbonReportRuleEngine(
    var steps: MutableList<ManualCarbonReportRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualCarbonReportRule): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualCarbonReportRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualCarbonReportRuleEngine(flags: Registry): ManualCarbonReportRuleEngine {
    return ManualCarbonReportRuleEngine(
        steps = mutableListOf<ManualCarbonReportRuleModifier>(
            TieredManualCarbonReportRuleAdjuster(
                mutableListOf<Double>(829.0, 1871.0, 8288.0, 9722.0),
                mutableListOf<Double>(0.98, 1.07, 1.13, 0.97),
            ),
            FlaggedManualCarbonReportRuleAdjuster(flags, "manual-carbon-report-rule-fallback", -8.7),
            PercentManualCarbonReportRuleAdjuster(-12.1),
            RoundingManualCarbonReportRuleAdjuster(0.05),
            ManualCarbonReportRuleMaximumOffsetAdjuster(1.41),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 59009.8,
    )
}
