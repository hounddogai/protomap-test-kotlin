package com.acme.transport.lastmile.locker.lockerassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive locker assignment calculation. */
interface AdaptiveLockerAssignmentModifier {
    fun describe(): String
    fun adjust(item: AdaptiveLockerAssignment, value: Double): Double
}

data class CapAdaptiveLockerAssignmentAdjuster(var cap: Double) : AdaptiveLockerAssignmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveLockerAssignment, value: Double): Double = minOf(value, cap)
}

data class FlaggedAdaptiveLockerAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveLockerAssignmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveLockerAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAdaptiveLockerAssignmentAdjuster(var minimum: Double) : AdaptiveLockerAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveLockerAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ChannelAdaptiveLockerAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveLockerAssignmentModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: AdaptiveLockerAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAdaptiveLockerAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveLockerAssignmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveLockerAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAdaptiveLockerAssignmentAdjuster(var percent: Double) : AdaptiveLockerAssignmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveLockerAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive locker assignment steps in order and keeps a trace of each. */
data class AdaptiveLockerAssignmentEngine(
    var steps: MutableList<AdaptiveLockerAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveLockerAssignment): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveLockerAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveLockerAssignmentEngine(flags: Registry): AdaptiveLockerAssignmentEngine {
    return AdaptiveLockerAssignmentEngine(
        steps = mutableListOf<AdaptiveLockerAssignmentModifier>(
            CapAdaptiveLockerAssignmentAdjuster(5907.0),
            FlaggedAdaptiveLockerAssignmentAdjuster(flags, "adaptive-locker-assignment-weighted", -8.1),
            MinimumAdaptiveLockerAssignmentAdjuster(14.2),
            ChannelAdaptiveLockerAssignmentAdjuster(
                mutableMapOf<String, Double>("online" to 1.18, "east" to 1.12, "north" to 0.82),
            ),
            TieredAdaptiveLockerAssignmentAdjuster(
                mutableListOf<Double>(4009.0, 5268.0, 8383.0),
                mutableListOf<Double>(1.18, 1.0, 1.08),
            ),
            PercentAdaptiveLockerAssignmentAdjuster(-13.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 29276.7,
    )
}
