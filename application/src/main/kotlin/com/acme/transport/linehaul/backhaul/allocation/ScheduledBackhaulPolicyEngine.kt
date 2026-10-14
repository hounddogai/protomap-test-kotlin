package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled backhaul policy calculation. */
interface ScheduledBackhaulPolicyModifier {
    fun describe(): String
    fun adjust(item: ScheduledBackhaulPolicy, value: Double): Double
}

data class TieredScheduledBackhaulPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledBackhaulPolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledBackhaulPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingScheduledBackhaulPolicyAdjuster(var step: Double) : ScheduledBackhaulPolicyModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledBackhaulPolicy, value: Double): Double = roundTo(value, step)
}

data class FlaggedScheduledBackhaulPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledBackhaulPolicyModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledBackhaulPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneScheduledBackhaulPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledBackhaulPolicyModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: ScheduledBackhaulPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled backhaul policy steps in order and keeps a trace of each. */
data class ScheduledBackhaulPolicyEngine(
    var steps: MutableList<ScheduledBackhaulPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledBackhaulPolicy): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledBackhaulPolicyModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledBackhaulPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledBackhaulPolicyEngine(flags: Registry): ScheduledBackhaulPolicyEngine {
    return ScheduledBackhaulPolicyEngine(
        steps = mutableListOf<ScheduledBackhaulPolicyModifier>(
            TieredScheduledBackhaulPolicyAdjuster(
                mutableListOf<Double>(3084.0, 9679.0),
                mutableListOf<Double>(0.97, 1.1),
            ),
            RoundingScheduledBackhaulPolicyAdjuster(0.5),
            FlaggedScheduledBackhaulPolicyAdjuster(flags, "scheduled-backhaul-policy-capped", -3.8),
            LaneScheduledBackhaulPolicyAdjuster(mutableMapOf<String, Double>("retail" to 1.3, "online" to 0.95)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 77244.3,
    )
}
