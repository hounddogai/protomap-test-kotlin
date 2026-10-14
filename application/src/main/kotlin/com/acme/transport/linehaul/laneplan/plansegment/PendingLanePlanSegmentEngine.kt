package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending lane plan segment calculation. */
interface PendingLanePlanSegmentStep {
    fun describe(): String
    fun adjust(item: PendingLanePlanSegment, value: Double): Double
}

data class SegmentPendingLanePlanSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingLanePlanSegmentStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: PendingLanePlanSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumPendingLanePlanSegmentAdjuster(var minimum: Double) : PendingLanePlanSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingLanePlanSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPendingLanePlanSegmentAdjuster(var percent: Double) : PendingLanePlanSegmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingLanePlanSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingLanePlanSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingLanePlanSegmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingLanePlanSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending lane plan segment steps in order and keeps a trace of each. */
data class PendingLanePlanSegmentEngine(
    var steps: MutableList<PendingLanePlanSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingLanePlanSegment): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingLanePlanSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingLanePlanSegmentEngine(flags: Registry): PendingLanePlanSegmentEngine {
    return PendingLanePlanSegmentEngine(
        steps = mutableListOf<PendingLanePlanSegmentStep>(
            SegmentPendingLanePlanSegmentAdjuster(
                mutableMapOf<String, Double>("retail" to 1.28, "east" to 0.81),
            ),
            MinimumPendingLanePlanSegmentAdjuster(30.6),
            PercentPendingLanePlanSegmentAdjuster(24.4),
            FlaggedPendingLanePlanSegmentAdjuster(flags, "pending-lane-plan-segment-strict", 1.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 81931.8,
    )
}
