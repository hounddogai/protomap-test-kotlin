package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual rate card milestone calculation. */
interface ManualRateCardMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: ManualRateCardMilestone, value: Double): Double
}

data class PercentManualRateCardMilestoneAdjuster(var percent: Double) : ManualRateCardMilestoneAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualRateCardMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedManualRateCardMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualRateCardMilestoneAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualRateCardMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumManualRateCardMilestoneAdjuster(var minimum: Double) : ManualRateCardMilestoneAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualRateCardMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingManualRateCardMilestoneAdjuster(var step: Double) : ManualRateCardMilestoneAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualRateCardMilestone, value: Double): Double = roundTo(value, step)
}

/** Runs the manual rate card milestone steps in order and keeps a trace of each. */
data class ManualRateCardMilestoneEngine(
    var steps: MutableList<ManualRateCardMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualRateCardMilestone): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualRateCardMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualRateCardMilestoneEngine(flags: Registry): ManualRateCardMilestoneEngine {
    return ManualRateCardMilestoneEngine(
        steps = mutableListOf<ManualRateCardMilestoneAdjuster>(
            PercentManualRateCardMilestoneAdjuster(-2.6),
            FlaggedManualRateCardMilestoneAdjuster(flags, "manual-rate-card-milestone-adaptive", 1.1),
            MinimumManualRateCardMilestoneAdjuster(11.0),
            RoundingManualRateCardMilestoneAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 62936.7,
    )
}
