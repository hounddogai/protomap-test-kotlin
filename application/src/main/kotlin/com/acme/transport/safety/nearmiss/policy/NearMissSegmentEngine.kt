package com.acme.transport.safety.nearmiss.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the near miss segment calculation. */
interface NearMissSegmentFactor {
    fun describe(): String
    fun adjust(item: NearMissSegment, value: Double): Double
}

data class RoundingNearMissSegmentAdjuster(var step: Double) : NearMissSegmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NearMissSegment, value: Double): Double = roundTo(value, step)
}

data class RegionNearMissSegmentAdjuster(var factors: MutableMap<String, Double>) : NearMissSegmentFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: NearMissSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedNearMissSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NearMissSegmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NearMissSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentNearMissSegmentAdjuster(var percent: Double) : NearMissSegmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapNearMissSegmentAdjuster(var cap: Double) : NearMissSegmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissSegment, value: Double): Double = minOf(value, cap)
}

data class NearMissSegmentEndsAtOffsetAdjuster(var weight: Double) : NearMissSegmentFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: NearMissSegment, value: Double): Double = value + item.endsAt.toDouble() * weight
}

/** Runs the near miss segment steps in order and keeps a trace of each. */
data class NearMissSegmentEngine(
    var steps: MutableList<NearMissSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: NearMissSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NearMissSegmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<NearMissSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultNearMissSegmentEngine(flags: Registry): NearMissSegmentEngine {
    return NearMissSegmentEngine(
        steps = mutableListOf<NearMissSegmentFactor>(
            RoundingNearMissSegmentAdjuster(0.25),
            RegionNearMissSegmentAdjuster(
                mutableMapOf<String, Double>("west" to 1.39, "north" to 0.72, "retail" to 1.37),
            ),
            FlaggedNearMissSegmentAdjuster(flags, "near-miss-segment-pending", 8.1),
            PercentNearMissSegmentAdjuster(-19.1),
            CapNearMissSegmentAdjuster(41684.0),
            NearMissSegmentEndsAtOffsetAdjuster(0.62),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 64651.6,
    )
}
