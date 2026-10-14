package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive service plan notice calculation. */
interface AdaptiveServicePlanNoticeAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveServicePlanNotice, value: Double): Double
}

data class RoundingAdaptiveServicePlanNoticeAdjuster(var step: Double) : AdaptiveServicePlanNoticeAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveServicePlanNotice, value: Double): Double = roundTo(value, step)
}

data class AdaptiveServicePlanNoticeExpiresAtOffsetAdjuster(
    var weight: Double,
) : AdaptiveServicePlanNoticeAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: AdaptiveServicePlanNotice, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class MinimumAdaptiveServicePlanNoticeAdjuster(var minimum: Double) : AdaptiveServicePlanNoticeAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveServicePlanNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocaleAdaptiveServicePlanNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveServicePlanNoticeAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: AdaptiveServicePlanNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive service plan notice steps in order and keeps a trace of each. */
data class AdaptiveServicePlanNoticeEngine(
    var steps: MutableList<AdaptiveServicePlanNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveServicePlanNotice): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveServicePlanNoticeAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveServicePlanNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveServicePlanNoticeEngine(): AdaptiveServicePlanNoticeEngine {
    return AdaptiveServicePlanNoticeEngine(
        steps = mutableListOf<AdaptiveServicePlanNoticeAdjuster>(
            RoundingAdaptiveServicePlanNoticeAdjuster(0.05),
            AdaptiveServicePlanNoticeExpiresAtOffsetAdjuster(0.24),
            MinimumAdaptiveServicePlanNoticeAdjuster(7.8),
            LocaleAdaptiveServicePlanNoticeAdjuster(mutableMapOf<String, Double>("online" to 1.2)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 63216.4,
    )
}
