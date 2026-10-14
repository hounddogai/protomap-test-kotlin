package com.acme.transport.lastmile.proofofdelivery.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted proof of delivery segment calculation. */
interface WeightedProofOfDeliverySegmentStep {
    fun describe(): String
    fun adjust(item: WeightedProofOfDeliverySegment, value: Double): Double
}

data class MinimumWeightedProofOfDeliverySegmentAdjuster(
    var minimum: Double,
) : WeightedProofOfDeliverySegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedProofOfDeliverySegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentWeightedProofOfDeliverySegmentAdjuster(
    var percent: Double,
) : WeightedProofOfDeliverySegmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedProofOfDeliverySegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class WeightedProofOfDeliverySegmentPriorityOffsetAdjuster(
    var weight: Double,
) : WeightedProofOfDeliverySegmentStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: WeightedProofOfDeliverySegment, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class CapWeightedProofOfDeliverySegmentAdjuster(var cap: Double) : WeightedProofOfDeliverySegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedProofOfDeliverySegment, value: Double): Double = minOf(value, cap)
}

data class RoundingWeightedProofOfDeliverySegmentAdjuster(
    var step: Double,
) : WeightedProofOfDeliverySegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedProofOfDeliverySegment, value: Double): Double = roundTo(value, step)
}

data class FlaggedWeightedProofOfDeliverySegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedProofOfDeliverySegmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedProofOfDeliverySegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted proof of delivery segment steps in order and keeps a trace of each. */
data class WeightedProofOfDeliverySegmentEngine(
    var steps: MutableList<WeightedProofOfDeliverySegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedProofOfDeliverySegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedProofOfDeliverySegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedProofOfDeliverySegmentEngine(flags: Registry): WeightedProofOfDeliverySegmentEngine {
    return WeightedProofOfDeliverySegmentEngine(
        steps = mutableListOf<WeightedProofOfDeliverySegmentStep>(
            MinimumWeightedProofOfDeliverySegmentAdjuster(41.4),
            PercentWeightedProofOfDeliverySegmentAdjuster(-2.4),
            WeightedProofOfDeliverySegmentPriorityOffsetAdjuster(0.82),
            CapWeightedProofOfDeliverySegmentAdjuster(29159.0),
            RoundingWeightedProofOfDeliverySegmentAdjuster(0.05),
            FlaggedWeightedProofOfDeliverySegmentAdjuster(
                flags,
                "weighted-proof-of-delivery-segment-scheduled",
                -2.0,
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 90255.1,
    )
}
