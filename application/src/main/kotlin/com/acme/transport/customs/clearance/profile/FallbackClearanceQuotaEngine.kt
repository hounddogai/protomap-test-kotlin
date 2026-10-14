package com.acme.transport.customs.clearance.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback clearance quota calculation. */
interface FallbackClearanceQuotaAdjuster {
    fun describe(): String
    fun adjust(item: FallbackClearanceQuota, value: Double): Double
}

data class LabelFallbackClearanceQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackClearanceQuotaAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackClearanceQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFallbackClearanceQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackClearanceQuotaAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackClearanceQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingFallbackClearanceQuotaAdjuster(var step: Double) : FallbackClearanceQuotaAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackClearanceQuota, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback clearance quota steps in order and keeps a trace of each. */
data class FallbackClearanceQuotaEngine(
    var steps: MutableList<FallbackClearanceQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackClearanceQuota): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackClearanceQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackClearanceQuotaEngine(flags: Registry): FallbackClearanceQuotaEngine {
    return FallbackClearanceQuotaEngine(
        steps = mutableListOf<FallbackClearanceQuotaAdjuster>(
            LabelFallbackClearanceQuotaAdjuster(mutableMapOf<String, Double>("west" to 0.74)),
            FlaggedFallbackClearanceQuotaAdjuster(flags, "fallback-clearance-quota-primary", 6.9),
            RoundingFallbackClearanceQuotaAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 82687.4,
    )
}
