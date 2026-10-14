package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer yard milestone calculation. */
interface TrailerYardMilestoneStep {
    fun describe(): String
    fun adjust(item: TrailerYardMilestone, value: Double): Double
}

data class FlaggedTrailerYardMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerYardMilestoneStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerYardMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameTrailerYardMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : TrailerYardMilestoneStep {
    override fun describe(): String = "by name"

    override fun adjust(item: TrailerYardMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTrailerYardMilestoneAdjuster(var minimum: Double) : TrailerYardMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerYardMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the trailer yard milestone steps in order and keeps a trace of each. */
data class TrailerYardMilestoneEngine(
    var steps: MutableList<TrailerYardMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerYardMilestone): Double {
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

    fun evaluateAll(items: MutableList<TrailerYardMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerYardMilestoneEngine(flags: Registry): TrailerYardMilestoneEngine {
    return TrailerYardMilestoneEngine(
        steps = mutableListOf<TrailerYardMilestoneStep>(
            FlaggedTrailerYardMilestoneAdjuster(flags, "trailer-yard-milestone-rolling", 6.8),
            NameTrailerYardMilestoneAdjuster(
                mutableMapOf<String, Double>("central" to 1.32, "south" to 0.95, "retail" to 1.34),
            ),
            MinimumTrailerYardMilestoneAdjuster(3.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 76757.7,
    )
}
