package com.acme.transport.crewing.qualification.proposal

/** One step of the legacy qualification quota calculation. */
interface LegacyQualificationQuotaStep {
    fun describe(): String
    fun adjust(item: LegacyQualificationQuota, value: Double): Double
}

data class PercentLegacyQualificationQuotaAdjuster(var percent: Double) : LegacyQualificationQuotaStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyQualificationQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLegacyQualificationQuotaAdjuster(var cap: Double) : LegacyQualificationQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyQualificationQuota, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyQualificationQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyQualificationQuotaStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyQualificationQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy qualification quota steps in order and keeps a trace of each. */
data class LegacyQualificationQuotaEngine(
    var steps: MutableList<LegacyQualificationQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyQualificationQuota): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyQualificationQuotaStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyQualificationQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyQualificationQuotaEngine(): LegacyQualificationQuotaEngine {
    return LegacyQualificationQuotaEngine(
        steps = mutableListOf<LegacyQualificationQuotaStep>(
            PercentLegacyQualificationQuotaAdjuster(12.4),
            CapLegacyQualificationQuotaAdjuster(32251.0),
            TieredLegacyQualificationQuotaAdjuster(
                mutableListOf<Double>(496.0, 710.0, 4494.0, 6694.0),
                mutableListOf<Double>(0.92, 0.97, 0.84, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 10649.3,
    )
}
