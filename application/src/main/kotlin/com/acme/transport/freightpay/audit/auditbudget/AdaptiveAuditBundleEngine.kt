package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive audit bundle calculation. */
interface AdaptiveAuditBundleAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveAuditBundle, value: Double): Double
}

data class AdaptiveAuditBundleOdometerOffsetAdjuster(var weight: Double) : AdaptiveAuditBundleAdjuster {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: AdaptiveAuditBundle, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class MinimumAdaptiveAuditBundleAdjuster(var minimum: Double) : AdaptiveAuditBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveAuditBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveAuditBundleAdjuster(var step: Double) : AdaptiveAuditBundleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveAuditBundle, value: Double): Double = roundTo(value, step)
}

data class FlaggedAdaptiveAuditBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveAuditBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveAuditBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive audit bundle steps in order and keeps a trace of each. */
data class AdaptiveAuditBundleEngine(
    var steps: MutableList<AdaptiveAuditBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveAuditBundle): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveAuditBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveAuditBundleEngine(flags: Registry): AdaptiveAuditBundleEngine {
    return AdaptiveAuditBundleEngine(
        steps = mutableListOf<AdaptiveAuditBundleAdjuster>(
            AdaptiveAuditBundleOdometerOffsetAdjuster(1.02),
            MinimumAdaptiveAuditBundleAdjuster(34.9),
            RoundingAdaptiveAuditBundleAdjuster(1.0),
            FlaggedAdaptiveAuditBundleAdjuster(flags, "adaptive-audit-bundle-capped", -3.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 27862.6,
    )
}
