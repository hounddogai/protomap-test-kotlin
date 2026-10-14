package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive proof of delivery schedule calculation. */
interface AdaptiveProofOfDeliveryScheduleAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveProofOfDeliverySchedule, value: Double): Double
}

data class NameAdaptiveProofOfDeliveryScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveProofOfDeliveryScheduleAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveProofOfDeliverySchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAdaptiveProofOfDeliveryScheduleAdjuster(
    var minimum: Double,
) : AdaptiveProofOfDeliveryScheduleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveProofOfDeliverySchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveProofOfDeliveryScheduleAdjuster(
    var step: Double,
) : AdaptiveProofOfDeliveryScheduleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveProofOfDeliverySchedule, value: Double): Double = roundTo(value, step)
}

data class PercentAdaptiveProofOfDeliveryScheduleAdjuster(
    var percent: Double,
) : AdaptiveProofOfDeliveryScheduleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveProofOfDeliverySchedule, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive proof of delivery schedule steps in order and keeps a trace of each. */
data class AdaptiveProofOfDeliveryScheduleEngine(
    var steps: MutableList<AdaptiveProofOfDeliveryScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveProofOfDeliverySchedule): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveProofOfDeliveryScheduleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveProofOfDeliverySchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveProofOfDeliveryScheduleEngine(): AdaptiveProofOfDeliveryScheduleEngine {
    return AdaptiveProofOfDeliveryScheduleEngine(
        steps = mutableListOf<AdaptiveProofOfDeliveryScheduleAdjuster>(
            NameAdaptiveProofOfDeliveryScheduleAdjuster(
                mutableMapOf<String, Double>("south" to 1.29, "west" to 1.08),
            ),
            MinimumAdaptiveProofOfDeliveryScheduleAdjuster(0.1),
            RoundingAdaptiveProofOfDeliveryScheduleAdjuster(0.25),
            PercentAdaptiveProofOfDeliveryScheduleAdjuster(-11.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 62456.6,
    )
}
