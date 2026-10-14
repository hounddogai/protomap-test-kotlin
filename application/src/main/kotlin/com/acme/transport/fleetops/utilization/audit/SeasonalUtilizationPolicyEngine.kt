package com.acme.transport.fleetops.utilization.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal utilization policy calculation. */
interface SeasonalUtilizationPolicyAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalUtilizationPolicy, value: Double): Double
}

data class SeasonalUtilizationPolicyRateOffsetAdjuster(var weight: Double) : SeasonalUtilizationPolicyAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: SeasonalUtilizationPolicy, value: Double): Double = value + item.rate * weight
}

data class FlaggedSeasonalUtilizationPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalUtilizationPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalUtilizationPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingSeasonalUtilizationPolicyAdjuster(var step: Double) : SeasonalUtilizationPolicyAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalUtilizationPolicy, value: Double): Double = roundTo(value, step)
}

data class PercentSeasonalUtilizationPolicyAdjuster(var percent: Double) : SeasonalUtilizationPolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalUtilizationPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal utilization policy steps in order and keeps a trace of each. */
data class SeasonalUtilizationPolicyEngine(
    var steps: MutableList<SeasonalUtilizationPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalUtilizationPolicy): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalUtilizationPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalUtilizationPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalUtilizationPolicyEngine(flags: Registry): SeasonalUtilizationPolicyEngine {
    return SeasonalUtilizationPolicyEngine(
        steps = mutableListOf<SeasonalUtilizationPolicyAdjuster>(
            SeasonalUtilizationPolicyRateOffsetAdjuster(1.77),
            FlaggedSeasonalUtilizationPolicyAdjuster(flags, "seasonal-utilization-policy-adaptive", -5.7),
            RoundingSeasonalUtilizationPolicyAdjuster(5.0),
            PercentSeasonalUtilizationPolicyAdjuster(13.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 54037.4,
    )
}
