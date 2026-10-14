package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.flags.Registry

/** One step of the trailer yard allocation calculation. */
interface TrailerYardAllocationStep {
    fun describe(): String
    fun adjust(item: TrailerYardAllocation, value: Double): Double
}

data class FlaggedTrailerYardAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerYardAllocationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerYardAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTrailerYardAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerYardAllocationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerYardAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTrailerYardAllocationAdjuster(var cap: Double) : TrailerYardAllocationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerYardAllocation, value: Double): Double = minOf(value, cap)
}

data class TrailerYardAllocationGraceDaysOffsetAdjuster(var weight: Double) : TrailerYardAllocationStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: TrailerYardAllocation, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class MinimumTrailerYardAllocationAdjuster(var minimum: Double) : TrailerYardAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerYardAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SegmentTrailerYardAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : TrailerYardAllocationStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: TrailerYardAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the trailer yard allocation steps in order and keeps a trace of each. */
data class TrailerYardAllocationEngine(
    var steps: MutableList<TrailerYardAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerYardAllocation): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerYardAllocationStep = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerYardAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerYardAllocationEngine(flags: Registry): TrailerYardAllocationEngine {
    return TrailerYardAllocationEngine(
        steps = mutableListOf<TrailerYardAllocationStep>(
            FlaggedTrailerYardAllocationAdjuster(flags, "trailer-yard-allocation-capped", 0.9),
            TieredTrailerYardAllocationAdjuster(
                mutableListOf<Double>(939.0, 1140.0, 8035.0),
                mutableListOf<Double>(0.83, 0.85, 0.95),
            ),
            CapTrailerYardAllocationAdjuster(13452.0),
            TrailerYardAllocationGraceDaysOffsetAdjuster(1.83),
            MinimumTrailerYardAllocationAdjuster(20.6),
            SegmentTrailerYardAllocationAdjuster(
                mutableMapOf<String, Double>("north" to 0.86, "online" to 0.78, "south" to 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 32003.6,
    )
}
