package com.acme.transport.emissions.idling.manifest

/** One step of the default idling segment calculation. */
interface DefaultIdlingSegmentAdjuster {
    fun describe(): String
    fun adjust(item: DefaultIdlingSegment, value: Double): Double
}

data class MinimumDefaultIdlingSegmentAdjuster(var minimum: Double) : DefaultIdlingSegmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultIdlingSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDefaultIdlingSegmentAdjuster(var cap: Double) : DefaultIdlingSegmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultIdlingSegment, value: Double): Double = minOf(value, cap)
}

data class DefaultIdlingSegmentPriorityOffsetAdjuster(var weight: Double) : DefaultIdlingSegmentAdjuster {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: DefaultIdlingSegment, value: Double): Double = value + item.priority.toDouble() * weight
}

data class TitleDefaultIdlingSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultIdlingSegmentAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: DefaultIdlingSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDefaultIdlingSegmentAdjuster(var percent: Double) : DefaultIdlingSegmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultIdlingSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default idling segment steps in order and keeps a trace of each. */
data class DefaultIdlingSegmentEngine(
    var steps: MutableList<DefaultIdlingSegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultIdlingSegment): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultIdlingSegmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultIdlingSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultIdlingSegmentEngine(): DefaultIdlingSegmentEngine {
    return DefaultIdlingSegmentEngine(
        steps = mutableListOf<DefaultIdlingSegmentAdjuster>(
            MinimumDefaultIdlingSegmentAdjuster(13.0),
            CapDefaultIdlingSegmentAdjuster(41917.0),
            DefaultIdlingSegmentPriorityOffsetAdjuster(1.92),
            TitleDefaultIdlingSegmentAdjuster(mutableMapOf<String, Double>("north" to 0.8, "west" to 1.39)),
            PercentDefaultIdlingSegmentAdjuster(-20.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 87579.8,
    )
}
