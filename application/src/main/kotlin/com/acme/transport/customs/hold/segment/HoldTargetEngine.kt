package com.acme.transport.customs.hold.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the hold target calculation. */
interface HoldTargetFactor {
    fun describe(): String
    fun adjust(item: HoldTarget, value: Double): Double
}

data class HoldTargetRevisionOffsetAdjuster(var weight: Double) : HoldTargetFactor {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: HoldTarget, value: Double): Double = value + item.revision.toDouble() * weight
}

data class MinimumHoldTargetAdjuster(var minimum: Double) : HoldTargetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HoldTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapHoldTargetAdjuster(var cap: Double) : HoldTargetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HoldTarget, value: Double): Double = minOf(value, cap)
}

data class FlaggedHoldTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingHoldTargetAdjuster(var step: Double) : HoldTargetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HoldTarget, value: Double): Double = roundTo(value, step)
}

data class NameHoldTargetAdjuster(var factors: MutableMap<String, Double>) : HoldTargetFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: HoldTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the hold target steps in order and keeps a trace of each. */
data class HoldTargetEngine(
    var steps: MutableList<HoldTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HoldTarget): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HoldTargetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<HoldTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHoldTargetEngine(flags: Registry): HoldTargetEngine {
    return HoldTargetEngine(
        steps = mutableListOf<HoldTargetFactor>(
            HoldTargetRevisionOffsetAdjuster(1.32),
            MinimumHoldTargetAdjuster(48.7),
            CapHoldTargetAdjuster(7687.0),
            FlaggedHoldTargetAdjuster(flags, "hold-target-capped", 8.2),
            RoundingHoldTargetAdjuster(1.0),
            NameHoldTargetAdjuster(
                mutableMapOf<String, Double>("north" to 1.38, "retail" to 1.15, "west" to 1.38),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 46027.9,
    )
}
