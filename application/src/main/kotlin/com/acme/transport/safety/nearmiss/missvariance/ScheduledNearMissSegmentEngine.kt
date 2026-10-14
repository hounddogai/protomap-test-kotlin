package com.acme.transport.safety.nearmiss.missvariance

/** One step of the scheduled near miss segment calculation. */
interface ScheduledNearMissSegmentModifier {
    fun describe(): String
    fun adjust(item: ScheduledNearMissSegment, value: Double): Double
}

data class ScheduledNearMissSegmentVersionOffsetAdjuster(
    var weight: Double,
) : ScheduledNearMissSegmentModifier {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: ScheduledNearMissSegment, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class TieredScheduledNearMissSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledNearMissSegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledNearMissSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentScheduledNearMissSegmentAdjuster(var percent: Double) : ScheduledNearMissSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledNearMissSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled near miss segment steps in order and keeps a trace of each. */
data class ScheduledNearMissSegmentEngine(
    var steps: MutableList<ScheduledNearMissSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledNearMissSegment): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledNearMissSegmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledNearMissSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledNearMissSegmentEngine(): ScheduledNearMissSegmentEngine {
    return ScheduledNearMissSegmentEngine(
        steps = mutableListOf<ScheduledNearMissSegmentModifier>(
            ScheduledNearMissSegmentVersionOffsetAdjuster(0.81),
            TieredScheduledNearMissSegmentAdjuster(
                mutableListOf<Double>(587.0, 4818.0, 8334.0),
                mutableListOf<Double>(0.93, 0.92, 1.02),
            ),
            PercentScheduledNearMissSegmentAdjuster(23.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 66987.6,
    )
}
