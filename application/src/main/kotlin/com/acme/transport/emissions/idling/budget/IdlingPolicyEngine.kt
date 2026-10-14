package com.acme.transport.emissions.idling.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the idling policy calculation. */
interface IdlingPolicyAdjuster {
    fun describe(): String
    fun adjust(item: IdlingPolicy, value: Double): Double
}

data class UnitIdlingPolicyAdjuster(var factors: MutableMap<String, Double>) : IdlingPolicyAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: IdlingPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class IdlingPolicyThresholdOffsetAdjuster(var weight: Double) : IdlingPolicyAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: IdlingPolicy, value: Double): Double = value + item.threshold * weight
}

data class RoundingIdlingPolicyAdjuster(var step: Double) : IdlingPolicyAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IdlingPolicy, value: Double): Double = roundTo(value, step)
}

data class PercentIdlingPolicyAdjuster(var percent: Double) : IdlingPolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapIdlingPolicyAdjuster(var cap: Double) : IdlingPolicyAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingPolicy, value: Double): Double = minOf(value, cap)
}

data class FlaggedIdlingPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the idling policy steps in order and keeps a trace of each. */
data class IdlingPolicyEngine(
    var steps: MutableList<IdlingPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: IdlingPolicy): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultIdlingPolicyEngine(flags: Registry): IdlingPolicyEngine {
    return IdlingPolicyEngine(
        steps = mutableListOf<IdlingPolicyAdjuster>(
            UnitIdlingPolicyAdjuster(
                mutableMapOf<String, Double>("east" to 1.35, "west" to 0.87, "south" to 0.84),
            ),
            IdlingPolicyThresholdOffsetAdjuster(1.4),
            RoundingIdlingPolicyAdjuster(1.0),
            PercentIdlingPolicyAdjuster(-14.0),
            CapIdlingPolicyAdjuster(11578.0),
            FlaggedIdlingPolicyAdjuster(flags, "idling-policy-manual", 8.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 1423.8,
    )
}
