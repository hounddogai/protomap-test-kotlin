package com.acme.transport.network.transittime.cycle

/** One step of the primary transit time adjustment calculation. */
interface PrimaryTransitTimeAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryTransitTimeAdjustment, value: Double): Double
}

data class PercentPrimaryTransitTimeAdjustmentAdjuster(
    var percent: Double,
) : PrimaryTransitTimeAdjustmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryTransitTimeAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPrimaryTransitTimeAdjustmentAdjuster(var cap: Double) : PrimaryTransitTimeAdjustmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryTransitTimeAdjustment, value: Double): Double = minOf(value, cap)
}

data class MinimumPrimaryTransitTimeAdjustmentAdjuster(
    var minimum: Double,
) : PrimaryTransitTimeAdjustmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryTransitTimeAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the primary transit time adjustment steps in order and keeps a trace of each. */
data class PrimaryTransitTimeAdjustmentEngine(
    var steps: MutableList<PrimaryTransitTimeAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryTransitTimeAdjustment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryTransitTimeAdjustmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryTransitTimeAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryTransitTimeAdjustmentEngine(): PrimaryTransitTimeAdjustmentEngine {
    return PrimaryTransitTimeAdjustmentEngine(
        steps = mutableListOf<PrimaryTransitTimeAdjustmentAdjuster>(
            PercentPrimaryTransitTimeAdjustmentAdjuster(-5.6),
            CapPrimaryTransitTimeAdjustmentAdjuster(35482.0),
            MinimumPrimaryTransitTimeAdjustmentAdjuster(25.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 36828.7,
    )
}
