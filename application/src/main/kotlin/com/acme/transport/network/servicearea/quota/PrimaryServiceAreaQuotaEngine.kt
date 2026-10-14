package com.acme.transport.network.servicearea.quota

import com.acme.transport.kit.flags.Registry

/** One step of the primary service area quota calculation. */
interface PrimaryServiceAreaQuotaStep {
    fun describe(): String
    fun adjust(item: PrimaryServiceAreaQuota, value: Double): Double
}

data class FlaggedPrimaryServiceAreaQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryServiceAreaQuotaStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryServiceAreaQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPrimaryServiceAreaQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryServiceAreaQuotaStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryServiceAreaQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapPrimaryServiceAreaQuotaAdjuster(var cap: Double) : PrimaryServiceAreaQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryServiceAreaQuota, value: Double): Double = minOf(value, cap)
}

/** Runs the primary service area quota steps in order and keeps a trace of each. */
data class PrimaryServiceAreaQuotaEngine(
    var steps: MutableList<PrimaryServiceAreaQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryServiceAreaQuota): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryServiceAreaQuotaStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryServiceAreaQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryServiceAreaQuotaEngine(flags: Registry): PrimaryServiceAreaQuotaEngine {
    return PrimaryServiceAreaQuotaEngine(
        steps = mutableListOf<PrimaryServiceAreaQuotaStep>(
            FlaggedPrimaryServiceAreaQuotaAdjuster(flags, "primary-service-area-quota-regional", 5.8),
            TieredPrimaryServiceAreaQuotaAdjuster(
                mutableListOf<Double>(3299.0, 8870.0, 9081.0),
                mutableListOf<Double>(0.82, 1.09, 0.87),
            ),
            CapPrimaryServiceAreaQuotaAdjuster(30303.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 99187.6,
    )
}
