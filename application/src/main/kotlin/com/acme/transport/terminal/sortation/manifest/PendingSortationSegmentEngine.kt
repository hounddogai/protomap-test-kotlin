package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending sortation segment calculation. */
interface PendingSortationSegmentAdjuster {
    fun describe(): String
    fun adjust(item: PendingSortationSegment, value: Double): Double
}

data class RoundingPendingSortationSegmentAdjuster(var step: Double) : PendingSortationSegmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingSortationSegment, value: Double): Double = roundTo(value, step)
}

data class PendingSortationSegmentExpiresAtOffsetAdjuster(
    var weight: Double,
) : PendingSortationSegmentAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: PendingSortationSegment, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class PercentPendingSortationSegmentAdjuster(var percent: Double) : PendingSortationSegmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingSortationSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPendingSortationSegmentAdjuster(var cap: Double) : PendingSortationSegmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingSortationSegment, value: Double): Double = minOf(value, cap)
}

data class FlaggedPendingSortationSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingSortationSegmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingSortationSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingSortationSegmentAdjuster(var minimum: Double) : PendingSortationSegmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingSortationSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending sortation segment steps in order and keeps a trace of each. */
data class PendingSortationSegmentEngine(
    var steps: MutableList<PendingSortationSegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingSortationSegment): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingSortationSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingSortationSegmentEngine(flags: Registry): PendingSortationSegmentEngine {
    return PendingSortationSegmentEngine(
        steps = mutableListOf<PendingSortationSegmentAdjuster>(
            RoundingPendingSortationSegmentAdjuster(0.5),
            PendingSortationSegmentExpiresAtOffsetAdjuster(1.99),
            PercentPendingSortationSegmentAdjuster(0.3),
            CapPendingSortationSegmentAdjuster(13089.0),
            FlaggedPendingSortationSegmentAdjuster(flags, "pending-sortation-segment-tiered", -4.1),
            MinimumPendingSortationSegmentAdjuster(7.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 87270.2,
    )
}
