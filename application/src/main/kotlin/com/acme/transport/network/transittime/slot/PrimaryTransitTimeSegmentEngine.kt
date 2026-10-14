package com.acme.transport.network.transittime.slot

/** One step of the primary transit time segment calculation. */
interface PrimaryTransitTimeSegmentModifier {
    fun describe(): String
    fun adjust(item: PrimaryTransitTimeSegment, value: Double): Double
}

data class MinimumPrimaryTransitTimeSegmentAdjuster(var minimum: Double) : PrimaryTransitTimeSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryTransitTimeSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPrimaryTransitTimeSegmentAdjuster(var cap: Double) : PrimaryTransitTimeSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryTransitTimeSegment, value: Double): Double = minOf(value, cap)
}

data class PrimaryTransitTimeSegmentBudgetOffsetAdjuster(
    var weight: Double,
) : PrimaryTransitTimeSegmentModifier {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: PrimaryTransitTimeSegment, value: Double): Double = value + item.budget * weight
}

/** Runs the primary transit time segment steps in order and keeps a trace of each. */
data class PrimaryTransitTimeSegmentEngine(
    var steps: MutableList<PrimaryTransitTimeSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryTransitTimeSegment): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryTransitTimeSegmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryTransitTimeSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryTransitTimeSegmentEngine(): PrimaryTransitTimeSegmentEngine {
    return PrimaryTransitTimeSegmentEngine(
        steps = mutableListOf<PrimaryTransitTimeSegmentModifier>(
            MinimumPrimaryTransitTimeSegmentAdjuster(10.9),
            CapPrimaryTransitTimeSegmentAdjuster(48335.0),
            PrimaryTransitTimeSegmentBudgetOffsetAdjuster(1.09),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 91877.2,
    )
}
