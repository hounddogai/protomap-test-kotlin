package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the idling segment calculation. */
interface IdlingSegmentFactor {
    fun describe(): String
    fun adjust(item: IdlingSegment, value: Double): Double
}

data class PercentIdlingSegmentAdjuster(var percent: Double) : IdlingSegmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredIdlingSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingSegmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class IdlingSegmentSequenceOffsetAdjuster(var weight: Double) : IdlingSegmentFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: IdlingSegment, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class FlaggedIdlingSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingSegmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameIdlingSegmentAdjuster(var factors: MutableMap<String, Double>) : IdlingSegmentFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: IdlingSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapIdlingSegmentAdjuster(var cap: Double) : IdlingSegmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingSegment, value: Double): Double = minOf(value, cap)
}

/** Runs the idling segment steps in order and keeps a trace of each. */
data class IdlingSegmentEngine(
    var steps: MutableList<IdlingSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: IdlingSegment): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IdlingSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultIdlingSegmentEngine(flags: Registry): IdlingSegmentEngine {
    return IdlingSegmentEngine(
        steps = mutableListOf<IdlingSegmentFactor>(
            PercentIdlingSegmentAdjuster(-4.6),
            TieredIdlingSegmentAdjuster(
                mutableListOf<Double>(1122.0, 5426.0),
                mutableListOf<Double>(1.18, 1.17),
            ),
            IdlingSegmentSequenceOffsetAdjuster(1.9),
            FlaggedIdlingSegmentAdjuster(flags, "idling-segment-primary", 8.0),
            NameIdlingSegmentAdjuster(
                mutableMapOf<String, Double>("south" to 1.01, "west" to 0.98, "retail" to 1.03),
            ),
            CapIdlingSegmentAdjuster(27028.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 86884.1,
    )
}
