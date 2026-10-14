package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient redelivery snapshot calculation. */
interface LenientRedeliverySnapshotFactor {
    fun describe(): String
    fun adjust(item: LenientRedeliverySnapshot, value: Double): Double
}

data class MinimumLenientRedeliverySnapshotAdjuster(var minimum: Double) : LenientRedeliverySnapshotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientRedeliverySnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLenientRedeliverySnapshotAdjuster(var percent: Double) : LenientRedeliverySnapshotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientRedeliverySnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLenientRedeliverySnapshotAdjuster(var step: Double) : LenientRedeliverySnapshotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientRedeliverySnapshot, value: Double): Double = roundTo(value, step)
}

/** Runs the lenient redelivery snapshot steps in order and keeps a trace of each. */
data class LenientRedeliverySnapshotEngine(
    var steps: MutableList<LenientRedeliverySnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientRedeliverySnapshot): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientRedeliverySnapshotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientRedeliverySnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientRedeliverySnapshotEngine(): LenientRedeliverySnapshotEngine {
    return LenientRedeliverySnapshotEngine(
        steps = mutableListOf<LenientRedeliverySnapshotFactor>(
            MinimumLenientRedeliverySnapshotAdjuster(46.9),
            PercentLenientRedeliverySnapshotAdjuster(9.1),
            RoundingLenientRedeliverySnapshotAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 52979.0,
    )
}
