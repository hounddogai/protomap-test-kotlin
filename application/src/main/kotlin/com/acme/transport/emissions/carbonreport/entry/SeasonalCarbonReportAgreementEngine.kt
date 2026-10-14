package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal carbon report agreement calculation. */
interface SeasonalCarbonReportAgreementAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalCarbonReportAgreement, value: Double): Double
}

data class PercentSeasonalCarbonReportAgreementAdjuster(
    var percent: Double,
) : SeasonalCarbonReportAgreementAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalCarbonReportAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapSeasonalCarbonReportAgreementAdjuster(var cap: Double) : SeasonalCarbonReportAgreementAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalCarbonReportAgreement, value: Double): Double = minOf(value, cap)
}

data class NameSeasonalCarbonReportAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalCarbonReportAgreementAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: SeasonalCarbonReportAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedSeasonalCarbonReportAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalCarbonReportAgreementAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalCarbonReportAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal carbon report agreement steps in order and keeps a trace of each. */
data class SeasonalCarbonReportAgreementEngine(
    var steps: MutableList<SeasonalCarbonReportAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalCarbonReportAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalCarbonReportAgreementAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalCarbonReportAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalCarbonReportAgreementEngine(flags: Registry): SeasonalCarbonReportAgreementEngine {
    return SeasonalCarbonReportAgreementEngine(
        steps = mutableListOf<SeasonalCarbonReportAgreementAdjuster>(
            PercentSeasonalCarbonReportAgreementAdjuster(17.6),
            CapSeasonalCarbonReportAgreementAdjuster(46914.0),
            NameSeasonalCarbonReportAgreementAdjuster(
                mutableMapOf<String, Double>("retail" to 1.34, "online" to 1.02, "north" to 0.87),
            ),
            FlaggedSeasonalCarbonReportAgreementAdjuster(
                flags,
                "seasonal-carbon-report-agreement-seasonal",
                5.3,
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 41686.2,
    )
}
