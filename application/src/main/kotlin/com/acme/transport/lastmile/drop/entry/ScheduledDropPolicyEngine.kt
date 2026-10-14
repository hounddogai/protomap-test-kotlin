package com.acme.transport.lastmile.drop.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled drop policy calculation. */
interface ScheduledDropPolicyStep {
    fun describe(): String
    fun adjust(item: ScheduledDropPolicy, value: Double): Double
}

data class FlaggedScheduledDropPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledDropPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledDropPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScheduledDropPolicyAdjuster(var percent: Double) : ScheduledDropPolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledDropPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ScheduledDropPolicyOdometerOffsetAdjuster(var weight: Double) : ScheduledDropPolicyStep {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: ScheduledDropPolicy, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class RoundingScheduledDropPolicyAdjuster(var step: Double) : ScheduledDropPolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledDropPolicy, value: Double): Double = roundTo(value, step)
}

data class NameScheduledDropPolicyAdjuster(var factors: MutableMap<String, Double>) : ScheduledDropPolicyStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ScheduledDropPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredScheduledDropPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledDropPolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledDropPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled drop policy steps in order and keeps a trace of each. */
data class ScheduledDropPolicyEngine(
    var steps: MutableList<ScheduledDropPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledDropPolicy): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledDropPolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledDropPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledDropPolicyEngine(flags: Registry): ScheduledDropPolicyEngine {
    return ScheduledDropPolicyEngine(
        steps = mutableListOf<ScheduledDropPolicyStep>(
            FlaggedScheduledDropPolicyAdjuster(flags, "scheduled-drop-policy-global", -5.2),
            PercentScheduledDropPolicyAdjuster(-0.2),
            ScheduledDropPolicyOdometerOffsetAdjuster(0.62),
            RoundingScheduledDropPolicyAdjuster(1.0),
            NameScheduledDropPolicyAdjuster(
                mutableMapOf<String, Double>("east" to 0.88, "west" to 1.0, "retail" to 0.72),
            ),
            TieredScheduledDropPolicyAdjuster(
                mutableListOf<Double>(1873.0, 6555.0, 7550.0),
                mutableListOf<Double>(1.03, 0.97, 0.95),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 3707.3,
    )
}
