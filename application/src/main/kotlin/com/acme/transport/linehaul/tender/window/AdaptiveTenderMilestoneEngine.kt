package com.acme.transport.linehaul.tender.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive tender milestone calculation. */
interface AdaptiveTenderMilestoneModifier {
    fun describe(): String
    fun adjust(item: AdaptiveTenderMilestone, value: Double): Double
}

data class AdaptiveTenderMilestoneRateOffsetAdjuster(var weight: Double) : AdaptiveTenderMilestoneModifier {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: AdaptiveTenderMilestone, value: Double): Double = value + item.rate * weight
}

data class MinimumAdaptiveTenderMilestoneAdjuster(var minimum: Double) : AdaptiveTenderMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveTenderMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAdaptiveTenderMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveTenderMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveTenderMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneAdaptiveTenderMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveTenderMilestoneModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: AdaptiveTenderMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive tender milestone steps in order and keeps a trace of each. */
data class AdaptiveTenderMilestoneEngine(
    var steps: MutableList<AdaptiveTenderMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveTenderMilestone): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveTenderMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveTenderMilestoneEngine(flags: Registry): AdaptiveTenderMilestoneEngine {
    return AdaptiveTenderMilestoneEngine(
        steps = mutableListOf<AdaptiveTenderMilestoneModifier>(
            AdaptiveTenderMilestoneRateOffsetAdjuster(1.24),
            MinimumAdaptiveTenderMilestoneAdjuster(13.3),
            FlaggedAdaptiveTenderMilestoneAdjuster(flags, "adaptive-tender-milestone-capped", 1.8),
            LaneAdaptiveTenderMilestoneAdjuster(mutableMapOf<String, Double>("east" to 0.71, "west" to 1.09)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 78375.2,
    )
}
