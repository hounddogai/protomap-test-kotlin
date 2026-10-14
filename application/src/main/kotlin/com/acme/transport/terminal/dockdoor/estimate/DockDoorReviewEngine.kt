package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.flags.Registry

/** One step of the dock door review calculation. */
interface DockDoorReviewFactor {
    fun describe(): String
    fun adjust(item: DockDoorReview, value: Double): Double
}

data class CapDockDoorReviewAdjuster(var cap: Double) : DockDoorReviewFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorReview, value: Double): Double = minOf(value, cap)
}

data class DockDoorReviewLimitOffsetAdjuster(var weight: Double) : DockDoorReviewFactor {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: DockDoorReview, value: Double): Double = value + item.limit.toDouble() * weight
}

data class MinimumDockDoorReviewAdjuster(var minimum: Double) : DockDoorReviewFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDockDoorReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorReviewFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDockDoorReviewAdjuster(var percent: Double) : DockDoorReviewFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the dock door review steps in order and keeps a trace of each. */
data class DockDoorReviewEngine(
    var steps: MutableList<DockDoorReviewFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DockDoorReview): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorReviewFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDockDoorReviewEngine(flags: Registry): DockDoorReviewEngine {
    return DockDoorReviewEngine(
        steps = mutableListOf<DockDoorReviewFactor>(
            CapDockDoorReviewAdjuster(48033.0),
            DockDoorReviewLimitOffsetAdjuster(1.49),
            MinimumDockDoorReviewAdjuster(17.8),
            FlaggedDockDoorReviewAdjuster(flags, "dock-door-review-pending", 6.1),
            PercentDockDoorReviewAdjuster(19.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 17591.0,
    )
}
