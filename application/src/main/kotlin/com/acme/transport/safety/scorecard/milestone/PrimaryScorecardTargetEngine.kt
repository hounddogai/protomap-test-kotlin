package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.flags.Registry

/** One step of the primary scorecard target calculation. */
interface PrimaryScorecardTargetAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryScorecardTarget, value: Double): Double
}

data class PrimaryScorecardTargetEtaMinutesOffsetAdjuster(var weight: Double) : PrimaryScorecardTargetAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: PrimaryScorecardTarget, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class MinimumPrimaryScorecardTargetAdjuster(var minimum: Double) : PrimaryScorecardTargetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryScorecardTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPrimaryScorecardTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryScorecardTargetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryScorecardTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelPrimaryScorecardTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryScorecardTargetAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: PrimaryScorecardTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary scorecard target steps in order and keeps a trace of each. */
data class PrimaryScorecardTargetEngine(
    var steps: MutableList<PrimaryScorecardTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryScorecardTarget): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryScorecardTargetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryScorecardTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryScorecardTargetEngine(flags: Registry): PrimaryScorecardTargetEngine {
    return PrimaryScorecardTargetEngine(
        steps = mutableListOf<PrimaryScorecardTargetAdjuster>(
            PrimaryScorecardTargetEtaMinutesOffsetAdjuster(0.81),
            MinimumPrimaryScorecardTargetAdjuster(5.2),
            FlaggedPrimaryScorecardTargetAdjuster(flags, "primary-scorecard-target-seasonal", 3.2),
            LabelPrimaryScorecardTargetAdjuster(
                mutableMapOf<String, Double>("retail" to 0.87, "online" to 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 84084.7,
    )
}
