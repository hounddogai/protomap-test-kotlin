package com.acme.transport.emissions.carbonreport.reportagreement

import com.acme.transport.kit.flags.Registry

/** One step of the fallback carbon report agreement calculation. */
interface FallbackCarbonReportAgreementAdjuster {
    fun describe(): String
    fun adjust(item: FallbackCarbonReportAgreement, value: Double): Double
}

data class TitleFallbackCarbonReportAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackCarbonReportAgreementAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: FallbackCarbonReportAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFallbackCarbonReportAgreementAdjuster(
    var minimum: Double,
) : FallbackCarbonReportAgreementAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackCarbonReportAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFallbackCarbonReportAgreementAdjuster(var cap: Double) : FallbackCarbonReportAgreementAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackCarbonReportAgreement, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackCarbonReportAgreementAdjuster(
    var percent: Double,
) : FallbackCarbonReportAgreementAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackCarbonReportAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredFallbackCarbonReportAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackCarbonReportAgreementAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackCarbonReportAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedFallbackCarbonReportAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackCarbonReportAgreementAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackCarbonReportAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback carbon report agreement steps in order and keeps a trace of each. */
data class FallbackCarbonReportAgreementEngine(
    var steps: MutableList<FallbackCarbonReportAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackCarbonReportAgreement): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackCarbonReportAgreementAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackCarbonReportAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackCarbonReportAgreementEngine(flags: Registry): FallbackCarbonReportAgreementEngine {
    return FallbackCarbonReportAgreementEngine(
        steps = mutableListOf<FallbackCarbonReportAgreementAdjuster>(
            TitleFallbackCarbonReportAgreementAdjuster(mutableMapOf<String, Double>("online" to 0.72)),
            MinimumFallbackCarbonReportAgreementAdjuster(12.3),
            CapFallbackCarbonReportAgreementAdjuster(44581.0),
            PercentFallbackCarbonReportAgreementAdjuster(9.6),
            TieredFallbackCarbonReportAgreementAdjuster(
                mutableListOf<Double>(12.0, 2738.0, 9834.0),
                mutableListOf<Double>(0.96, 0.88, 1.09),
            ),
            FlaggedFallbackCarbonReportAgreementAdjuster(
                flags,
                "fallback-carbon-report-agreement-primary",
                -2.4,
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 99250.8,
    )
}
