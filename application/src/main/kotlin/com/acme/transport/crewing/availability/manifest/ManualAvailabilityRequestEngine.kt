package com.acme.transport.crewing.availability.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual availability request calculation. */
interface ManualAvailabilityRequestAdjuster {
    fun describe(): String
    fun adjust(item: ManualAvailabilityRequest, value: Double): Double
}

data class RoundingManualAvailabilityRequestAdjuster(var step: Double) : ManualAvailabilityRequestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualAvailabilityRequest, value: Double): Double = roundTo(value, step)
}

data class CapManualAvailabilityRequestAdjuster(var cap: Double) : ManualAvailabilityRequestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualAvailabilityRequest, value: Double): Double = minOf(value, cap)
}

data class ManualAvailabilityRequestMarginOffsetAdjuster(
    var weight: Double,
) : ManualAvailabilityRequestAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: ManualAvailabilityRequest, value: Double): Double = value + item.margin * weight
}

data class TitleManualAvailabilityRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualAvailabilityRequestAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualAvailabilityRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedManualAvailabilityRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualAvailabilityRequestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualAvailabilityRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumManualAvailabilityRequestAdjuster(var minimum: Double) : ManualAvailabilityRequestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualAvailabilityRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual availability request steps in order and keeps a trace of each. */
data class ManualAvailabilityRequestEngine(
    var steps: MutableList<ManualAvailabilityRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualAvailabilityRequest): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualAvailabilityRequestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualAvailabilityRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualAvailabilityRequestEngine(flags: Registry): ManualAvailabilityRequestEngine {
    return ManualAvailabilityRequestEngine(
        steps = mutableListOf<ManualAvailabilityRequestAdjuster>(
            RoundingManualAvailabilityRequestAdjuster(0.25),
            CapManualAvailabilityRequestAdjuster(30607.0),
            ManualAvailabilityRequestMarginOffsetAdjuster(1.77),
            TitleManualAvailabilityRequestAdjuster(
                mutableMapOf<String, Double>("south" to 1.1, "retail" to 0.93),
            ),
            FlaggedManualAvailabilityRequestAdjuster(flags, "manual-availability-request-adaptive", 4.8),
            MinimumManualAvailabilityRequestAdjuster(30.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 24356.8,
    )
}
