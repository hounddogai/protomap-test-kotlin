package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.flags.Registry

/** One step of the primary tender segment calculation. */
interface PrimaryTenderSegmentAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryTenderSegment, value: Double): Double
}

data class LanePrimaryTenderSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryTenderSegmentAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: PrimaryTenderSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPrimaryTenderSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryTenderSegmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryTenderSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPrimaryTenderSegmentAdjuster(var cap: Double) : PrimaryTenderSegmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryTenderSegment, value: Double): Double = minOf(value, cap)
}

data class MinimumPrimaryTenderSegmentAdjuster(var minimum: Double) : PrimaryTenderSegmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryTenderSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PrimaryTenderSegmentFloorValueOffsetAdjuster(var weight: Double) : PrimaryTenderSegmentAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: PrimaryTenderSegment, value: Double): Double = value + item.floorValue * weight
}

/** Runs the primary tender segment steps in order and keeps a trace of each. */
data class PrimaryTenderSegmentEngine(
    var steps: MutableList<PrimaryTenderSegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryTenderSegment): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryTenderSegmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryTenderSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryTenderSegmentEngine(flags: Registry): PrimaryTenderSegmentEngine {
    return PrimaryTenderSegmentEngine(
        steps = mutableListOf<PrimaryTenderSegmentAdjuster>(
            LanePrimaryTenderSegmentAdjuster(
                mutableMapOf<String, Double>("online" to 1.2, "east" to 0.73, "north" to 1.02),
            ),
            FlaggedPrimaryTenderSegmentAdjuster(flags, "primary-tender-segment-legacy", 5.6),
            CapPrimaryTenderSegmentAdjuster(20460.0),
            MinimumPrimaryTenderSegmentAdjuster(26.8),
            PrimaryTenderSegmentFloorValueOffsetAdjuster(0.37),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 33083.6,
    )
}
