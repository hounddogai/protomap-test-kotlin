package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive ev charge variance calculation. */
interface AdaptiveEvChargeVarianceAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveEvChargeVariance, value: Double): Double
}

data class NameAdaptiveEvChargeVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveEvChargeVarianceAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveEvChargeVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAdaptiveEvChargeVarianceAdjuster(var percent: Double) : AdaptiveEvChargeVarianceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveEvChargeVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAdaptiveEvChargeVarianceAdjuster(var minimum: Double) : AdaptiveEvChargeVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveEvChargeVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAdaptiveEvChargeVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveEvChargeVarianceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveEvChargeVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive ev charge variance steps in order and keeps a trace of each. */
data class AdaptiveEvChargeVarianceEngine(
    var steps: MutableList<AdaptiveEvChargeVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveEvChargeVariance): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveEvChargeVarianceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveEvChargeVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveEvChargeVarianceEngine(flags: Registry): AdaptiveEvChargeVarianceEngine {
    return AdaptiveEvChargeVarianceEngine(
        steps = mutableListOf<AdaptiveEvChargeVarianceAdjuster>(
            NameAdaptiveEvChargeVarianceAdjuster(
                mutableMapOf<String, Double>("south" to 1.18, "retail" to 1.18, "central" to 1.32),
            ),
            PercentAdaptiveEvChargeVarianceAdjuster(13.9),
            MinimumAdaptiveEvChargeVarianceAdjuster(11.5),
            FlaggedAdaptiveEvChargeVarianceAdjuster(flags, "adaptive-ev-charge-variance-fallback", 6.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 9260.4,
    )
}
