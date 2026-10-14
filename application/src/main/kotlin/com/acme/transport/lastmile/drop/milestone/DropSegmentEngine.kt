package com.acme.transport.lastmile.drop.milestone

/** One step of the drop segment calculation. */
interface DropSegmentStep {
    fun describe(): String
    fun adjust(item: DropSegment, value: Double): Double
}

data class MinimumDropSegmentAdjuster(var minimum: Double) : DropSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DropSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDropSegmentAdjuster(var cap: Double) : DropSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DropSegment, value: Double): Double = minOf(value, cap)
}

data class PercentDropSegmentAdjuster(var percent: Double) : DropSegmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DropSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDropSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DropSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DropSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the drop segment steps in order and keeps a trace of each. */
data class DropSegmentEngine(
    var steps: MutableList<DropSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DropSegment): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DropSegmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<DropSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDropSegmentEngine(): DropSegmentEngine {
    return DropSegmentEngine(
        steps = mutableListOf<DropSegmentStep>(
            MinimumDropSegmentAdjuster(40.1),
            CapDropSegmentAdjuster(1375.0),
            PercentDropSegmentAdjuster(2.5),
            TieredDropSegmentAdjuster(mutableListOf<Double>(414.0, 9371.0), mutableListOf<Double>(1.08, 1.17)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 90128.2,
    )
}
