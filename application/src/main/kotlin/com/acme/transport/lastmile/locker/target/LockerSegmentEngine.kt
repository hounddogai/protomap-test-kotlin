package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.numbers.roundTo

/** One step of the locker segment calculation. */
interface LockerSegmentFactor {
    fun describe(): String
    fun adjust(item: LockerSegment, value: Double): Double
}

data class RoundingLockerSegmentAdjuster(var step: Double) : LockerSegmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LockerSegment, value: Double): Double = roundTo(value, step)
}

data class PercentLockerSegmentAdjuster(var percent: Double) : LockerSegmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LockerSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLockerSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerSegmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the locker segment steps in order and keeps a trace of each. */
data class LockerSegmentEngine(
    var steps: MutableList<LockerSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LockerSegment): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerSegmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LockerSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLockerSegmentEngine(): LockerSegmentEngine {
    return LockerSegmentEngine(
        steps = mutableListOf<LockerSegmentFactor>(
            RoundingLockerSegmentAdjuster(0.25),
            PercentLockerSegmentAdjuster(-21.6),
            TieredLockerSegmentAdjuster(
                mutableListOf<Double>(231.0, 697.0, 2121.0, 9425.0),
                mutableListOf<Double>(0.84, 0.92, 1.07, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 20157.5,
    )
}
