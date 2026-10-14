package com.acme.transport.network.servicearea.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient service area milestone calculation. */
interface LenientServiceAreaMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: LenientServiceAreaMilestone, value: Double): Double
}

data class PercentLenientServiceAreaMilestoneAdjuster(
    var percent: Double,
) : LenientServiceAreaMilestoneAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientServiceAreaMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLenientServiceAreaMilestoneAdjuster(var step: Double) : LenientServiceAreaMilestoneAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientServiceAreaMilestone, value: Double): Double = roundTo(value, step)
}

data class FlaggedLenientServiceAreaMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientServiceAreaMilestoneAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientServiceAreaMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DepotLenientServiceAreaMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientServiceAreaMilestoneAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: LenientServiceAreaMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient service area milestone steps in order and keeps a trace of each. */
data class LenientServiceAreaMilestoneEngine(
    var steps: MutableList<LenientServiceAreaMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientServiceAreaMilestone): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientServiceAreaMilestoneAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientServiceAreaMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientServiceAreaMilestoneEngine(flags: Registry): LenientServiceAreaMilestoneEngine {
    return LenientServiceAreaMilestoneEngine(
        steps = mutableListOf<LenientServiceAreaMilestoneAdjuster>(
            PercentLenientServiceAreaMilestoneAdjuster(-8.3),
            RoundingLenientServiceAreaMilestoneAdjuster(0.25),
            FlaggedLenientServiceAreaMilestoneAdjuster(flags, "lenient-service-area-milestone-lenient", 6.9),
            DepotLenientServiceAreaMilestoneAdjuster(mutableMapOf<String, Double>("central" to 1.11)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 57542.4,
    )
}
