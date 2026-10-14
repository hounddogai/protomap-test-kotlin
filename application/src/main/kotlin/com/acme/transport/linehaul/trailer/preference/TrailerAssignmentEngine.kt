package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer assignment calculation. */
interface TrailerAssignmentFactor {
    fun describe(): String
    fun adjust(item: TrailerAssignment, value: Double): Double
}

data class UnitTrailerAssignmentAdjuster(var factors: MutableMap<String, Double>) : TrailerAssignmentFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: TrailerAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTrailerAssignmentAdjuster(var minimum: Double) : TrailerAssignmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTrailerAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTrailerAssignmentAdjuster(var percent: Double) : TrailerAssignmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingTrailerAssignmentAdjuster(var step: Double) : TrailerAssignmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerAssignment, value: Double): Double = roundTo(value, step)
}

data class TrailerAssignmentStartsAtOffsetAdjuster(var weight: Double) : TrailerAssignmentFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: TrailerAssignment, value: Double): Double = value + item.startsAt.toDouble() * weight
}

/** Runs the trailer assignment steps in order and keeps a trace of each. */
data class TrailerAssignmentEngine(
    var steps: MutableList<TrailerAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerAssignment): Double {
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

    fun evaluateAll(items: MutableList<TrailerAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerAssignmentEngine(flags: Registry): TrailerAssignmentEngine {
    return TrailerAssignmentEngine(
        steps = mutableListOf<TrailerAssignmentFactor>(
            UnitTrailerAssignmentAdjuster(mutableMapOf<String, Double>("east" to 1.29)),
            MinimumTrailerAssignmentAdjuster(36.9),
            FlaggedTrailerAssignmentAdjuster(flags, "trailer-assignment-global", 9.7),
            PercentTrailerAssignmentAdjuster(-15.2),
            RoundingTrailerAssignmentAdjuster(0.5),
            TrailerAssignmentStartsAtOffsetAdjuster(1.13),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 57849.7,
    )
}
