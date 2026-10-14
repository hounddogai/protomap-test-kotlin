package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive ev charge quota calculation. */
interface AdaptiveEvChargeQuotaStep {
    fun describe(): String
    fun adjust(item: AdaptiveEvChargeQuota, value: Double): Double
}

data class AdaptiveEvChargeQuotaThresholdOffsetAdjuster(var weight: Double) : AdaptiveEvChargeQuotaStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: AdaptiveEvChargeQuota, value: Double): Double = value + item.threshold * weight
}

data class FlaggedAdaptiveEvChargeQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveEvChargeQuotaStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveEvChargeQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAdaptiveEvChargeQuotaAdjuster(var step: Double) : AdaptiveEvChargeQuotaStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveEvChargeQuota, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive ev charge quota steps in order and keeps a trace of each. */
data class AdaptiveEvChargeQuotaEngine(
    var steps: MutableList<AdaptiveEvChargeQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveEvChargeQuota): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveEvChargeQuotaStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveEvChargeQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveEvChargeQuotaEngine(flags: Registry): AdaptiveEvChargeQuotaEngine {
    return AdaptiveEvChargeQuotaEngine(
        steps = mutableListOf<AdaptiveEvChargeQuotaStep>(
            AdaptiveEvChargeQuotaThresholdOffsetAdjuster(0.49),
            FlaggedAdaptiveEvChargeQuotaAdjuster(flags, "adaptive-ev-charge-quota-primary", -6.0),
            RoundingAdaptiveEvChargeQuotaAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 74735.2,
    )
}
