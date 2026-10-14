package com.acme.transport.crewing.roster.quota

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the roster request calculation. */
interface RosterRequestAdjuster {
    fun describe(): String
    fun adjust(item: RosterRequest, value: Double): Double
}

data class LabelRosterRequestAdjuster(var factors: MutableMap<String, Double>) : RosterRequestAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: RosterRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRosterRequestAdjuster(var step: Double) : RosterRequestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterRequest, value: Double): Double = roundTo(value, step)
}

data class CapRosterRequestAdjuster(var cap: Double) : RosterRequestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterRequest, value: Double): Double = minOf(value, cap)
}

data class PercentRosterRequestAdjuster(var percent: Double) : RosterRequestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RosterRequestCeilingOffsetAdjuster(var weight: Double) : RosterRequestAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: RosterRequest, value: Double): Double = value + item.ceiling * weight
}

data class MinimumRosterRequestAdjuster(var minimum: Double) : RosterRequestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RosterRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the roster request steps in order and keeps a trace of each. */
data class RosterRequestEngine(
    var steps: MutableList<RosterRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RosterRequest): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RosterRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRosterRequestEngine(): RosterRequestEngine {
    return RosterRequestEngine(
        steps = mutableListOf<RosterRequestAdjuster>(
            LabelRosterRequestAdjuster(
                mutableMapOf<String, Double>("south" to 1.09, "west" to 1.36, "central" to 1.15),
            ),
            RoundingRosterRequestAdjuster(0.05),
            CapRosterRequestAdjuster(48672.0),
            PercentRosterRequestAdjuster(-14.7),
            RosterRequestCeilingOffsetAdjuster(1.48),
            MinimumRosterRequestAdjuster(27.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 61951.8,
    )
}
