package com.acme.transport.emissions.carbonreport.agreement

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the carbon report agreement calculation. */
interface CarbonReportAgreementStep {
    fun describe(): String
    fun adjust(item: CarbonReportAgreement, value: Double): Double
}

data class TitleCarbonReportAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : CarbonReportAgreementStep {
    override fun describe(): String = "by title"

    override fun adjust(item: CarbonReportAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCarbonReportAgreementAdjuster(var percent: Double) : CarbonReportAgreementStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCarbonReportAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CarbonReportAgreementStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CarbonReportAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the carbon report agreement steps in order and keeps a trace of each. */
data class CarbonReportAgreementEngine(
    var steps: MutableList<CarbonReportAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CarbonReportAgreement): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CarbonReportAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCarbonReportAgreementEngine(): CarbonReportAgreementEngine {
    return CarbonReportAgreementEngine(
        steps = mutableListOf<CarbonReportAgreementStep>(
            TitleCarbonReportAgreementAdjuster(mutableMapOf<String, Double>("central" to 0.85, "south" to 1.11)),
            PercentCarbonReportAgreementAdjuster(-16.5),
            TieredCarbonReportAgreementAdjuster(
                mutableListOf<Double>(5412.0, 6583.0, 7097.0),
                mutableListOf<Double>(1.14, 0.9, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 26549.4,
    )
}
