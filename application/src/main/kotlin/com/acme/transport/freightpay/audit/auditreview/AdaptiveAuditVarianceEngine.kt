package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive audit variance calculation. */
interface AdaptiveAuditVarianceFactor {
    fun describe(): String
    fun adjust(item: AdaptiveAuditVariance, value: Double): Double
}

data class RegionAdaptiveAuditVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveAuditVarianceFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: AdaptiveAuditVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AdaptiveAuditVarianceWindowDaysOffsetAdjuster(var weight: Double) : AdaptiveAuditVarianceFactor {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: AdaptiveAuditVariance, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class MinimumAdaptiveAuditVarianceAdjuster(var minimum: Double) : AdaptiveAuditVarianceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveAuditVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveAuditVarianceAdjuster(var step: Double) : AdaptiveAuditVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveAuditVariance, value: Double): Double = roundTo(value, step)
}

data class FlaggedAdaptiveAuditVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveAuditVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveAuditVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive audit variance steps in order and keeps a trace of each. */
data class AdaptiveAuditVarianceEngine(
    var steps: MutableList<AdaptiveAuditVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveAuditVariance): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveAuditVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveAuditVarianceEngine(flags: Registry): AdaptiveAuditVarianceEngine {
    return AdaptiveAuditVarianceEngine(
        steps = mutableListOf<AdaptiveAuditVarianceFactor>(
            RegionAdaptiveAuditVarianceAdjuster(mutableMapOf<String, Double>("online" to 1.33, "south" to 1.38)),
            AdaptiveAuditVarianceWindowDaysOffsetAdjuster(0.66),
            MinimumAdaptiveAuditVarianceAdjuster(20.6),
            RoundingAdaptiveAuditVarianceAdjuster(0.1),
            FlaggedAdaptiveAuditVarianceAdjuster(flags, "adaptive-audit-variance-seasonal", -7.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 36158.7,
    )
}
