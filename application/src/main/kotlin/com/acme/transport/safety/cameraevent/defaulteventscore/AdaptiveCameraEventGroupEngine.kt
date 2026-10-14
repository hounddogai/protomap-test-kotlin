package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive camera event group calculation. */
interface AdaptiveCameraEventGroupModifier {
    fun describe(): String
    fun adjust(item: AdaptiveCameraEventGroup, value: Double): Double
}

data class FlaggedAdaptiveCameraEventGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveCameraEventGroupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveCameraEventGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAdaptiveCameraEventGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveCameraEventGroupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveCameraEventGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAdaptiveCameraEventGroupAdjuster(var minimum: Double) : AdaptiveCameraEventGroupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveCameraEventGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AdaptiveCameraEventGroupRatioOffsetAdjuster(var weight: Double) : AdaptiveCameraEventGroupModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: AdaptiveCameraEventGroup, value: Double): Double = value + item.ratio * weight
}

data class RoundingAdaptiveCameraEventGroupAdjuster(var step: Double) : AdaptiveCameraEventGroupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveCameraEventGroup, value: Double): Double = roundTo(value, step)
}

data class TitleAdaptiveCameraEventGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveCameraEventGroupModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveCameraEventGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive camera event group steps in order and keeps a trace of each. */
data class AdaptiveCameraEventGroupEngine(
    var steps: MutableList<AdaptiveCameraEventGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveCameraEventGroup): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveCameraEventGroupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveCameraEventGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveCameraEventGroupEngine(flags: Registry): AdaptiveCameraEventGroupEngine {
    return AdaptiveCameraEventGroupEngine(
        steps = mutableListOf<AdaptiveCameraEventGroupModifier>(
            FlaggedAdaptiveCameraEventGroupAdjuster(flags, "adaptive-camera-event-group-weighted", 8.4),
            TieredAdaptiveCameraEventGroupAdjuster(
                mutableListOf<Double>(5357.0, 7080.0, 8295.0),
                mutableListOf<Double>(1.0, 1.07, 1.19),
            ),
            MinimumAdaptiveCameraEventGroupAdjuster(33.3),
            AdaptiveCameraEventGroupRatioOffsetAdjuster(0.58),
            RoundingAdaptiveCameraEventGroupAdjuster(0.1),
            TitleAdaptiveCameraEventGroupAdjuster(
                mutableMapOf<String, Double>("retail" to 0.93, "online" to 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 82702.0,
    )
}
