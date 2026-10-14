package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive backhaul quota calculation. */
interface AdaptiveBackhaulQuotaAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveBackhaulQuota, value: Double): Double
}

data class MinimumAdaptiveBackhaulQuotaAdjuster(var minimum: Double) : AdaptiveBackhaulQuotaAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveBackhaulQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAdaptiveBackhaulQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveBackhaulQuotaAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveBackhaulQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameAdaptiveBackhaulQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveBackhaulQuotaAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveBackhaulQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAdaptiveBackhaulQuotaAdjuster(var percent: Double) : AdaptiveBackhaulQuotaAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveBackhaulQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive backhaul quota steps in order and keeps a trace of each. */
data class AdaptiveBackhaulQuotaEngine(
    var steps: MutableList<AdaptiveBackhaulQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveBackhaulQuota): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveBackhaulQuotaAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveBackhaulQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveBackhaulQuotaEngine(flags: Registry): AdaptiveBackhaulQuotaEngine {
    return AdaptiveBackhaulQuotaEngine(
        steps = mutableListOf<AdaptiveBackhaulQuotaAdjuster>(
            MinimumAdaptiveBackhaulQuotaAdjuster(10.6),
            FlaggedAdaptiveBackhaulQuotaAdjuster(flags, "adaptive-backhaul-quota-rolling", -0.6),
            NameAdaptiveBackhaulQuotaAdjuster(mutableMapOf<String, Double>("retail" to 1.21)),
            PercentAdaptiveBackhaulQuotaAdjuster(1.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 4694.3,
    )
}
