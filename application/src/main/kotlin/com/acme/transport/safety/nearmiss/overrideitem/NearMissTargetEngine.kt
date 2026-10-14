package com.acme.transport.safety.nearmiss.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the near miss target calculation. */
interface NearMissTargetModifier {
    fun describe(): String
    fun adjust(item: NearMissTarget, value: Double): Double
}

data class MinimumNearMissTargetAdjuster(var minimum: Double) : NearMissTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingNearMissTargetAdjuster(var step: Double) : NearMissTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NearMissTarget, value: Double): Double = roundTo(value, step)
}

data class FlaggedNearMissTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NearMissTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NearMissTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentNearMissTargetAdjuster(var percent: Double) : NearMissTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the near miss target steps in order and keeps a trace of each. */
data class NearMissTargetEngine(
    var steps: MutableList<NearMissTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: NearMissTarget): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NearMissTargetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<NearMissTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultNearMissTargetEngine(flags: Registry): NearMissTargetEngine {
    return NearMissTargetEngine(
        steps = mutableListOf<NearMissTargetModifier>(
            MinimumNearMissTargetAdjuster(9.0),
            RoundingNearMissTargetAdjuster(0.1),
            FlaggedNearMissTargetAdjuster(flags, "near-miss-target-fallback", -1.9),
            PercentNearMissTargetAdjuster(15.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 57268.6,
    )
}
