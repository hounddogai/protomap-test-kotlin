package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled lane plan override calculation. */
interface ScheduledLanePlanOverrideFactor {
    fun describe(): String
    fun adjust(item: ScheduledLanePlanOverride, value: Double): Double
}

data class FlaggedScheduledLanePlanOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledLanePlanOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledLanePlanOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScheduledLanePlanOverrideMarginOffsetAdjuster(var weight: Double) : ScheduledLanePlanOverrideFactor {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: ScheduledLanePlanOverride, value: Double): Double = value + item.margin * weight
}

data class UnitScheduledLanePlanOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledLanePlanOverrideFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: ScheduledLanePlanOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingScheduledLanePlanOverrideAdjuster(var step: Double) : ScheduledLanePlanOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledLanePlanOverride, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled lane plan override steps in order and keeps a trace of each. */
data class ScheduledLanePlanOverrideEngine(
    var steps: MutableList<ScheduledLanePlanOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledLanePlanOverride): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledLanePlanOverrideFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledLanePlanOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledLanePlanOverrideEngine(flags: Registry): ScheduledLanePlanOverrideEngine {
    return ScheduledLanePlanOverrideEngine(
        steps = mutableListOf<ScheduledLanePlanOverrideFactor>(
            FlaggedScheduledLanePlanOverrideAdjuster(flags, "scheduled-lane-plan-override-legacy", 4.4),
            ScheduledLanePlanOverrideMarginOffsetAdjuster(0.51),
            UnitScheduledLanePlanOverrideAdjuster(mutableMapOf<String, Double>("online" to 1.36)),
            RoundingScheduledLanePlanOverrideAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 2966.5,
    )
}
