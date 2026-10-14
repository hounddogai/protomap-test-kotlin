package com.acme.transport.customs.hold.holdmapping

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled hold mapping calculation. */
interface ScheduledHoldMappingAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledHoldMapping, value: Double): Double
}

data class CapScheduledHoldMappingAdjuster(var cap: Double) : ScheduledHoldMappingAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledHoldMapping, value: Double): Double = minOf(value, cap)
}

data class LabelScheduledHoldMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledHoldMappingAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: ScheduledHoldMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentScheduledHoldMappingAdjuster(var percent: Double) : ScheduledHoldMappingAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledHoldMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ScheduledHoldMappingStartsAtOffsetAdjuster(var weight: Double) : ScheduledHoldMappingAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: ScheduledHoldMapping, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class FlaggedScheduledHoldMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledHoldMappingAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledHoldMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled hold mapping steps in order and keeps a trace of each. */
data class ScheduledHoldMappingEngine(
    var steps: MutableList<ScheduledHoldMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledHoldMapping): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledHoldMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledHoldMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledHoldMappingEngine(flags: Registry): ScheduledHoldMappingEngine {
    return ScheduledHoldMappingEngine(
        steps = mutableListOf<ScheduledHoldMappingAdjuster>(
            CapScheduledHoldMappingAdjuster(5652.0),
            LabelScheduledHoldMappingAdjuster(mutableMapOf<String, Double>("retail" to 0.87)),
            PercentScheduledHoldMappingAdjuster(9.6),
            ScheduledHoldMappingStartsAtOffsetAdjuster(0.43),
            FlaggedScheduledHoldMappingAdjuster(flags, "scheduled-hold-mapping-global", 0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 78931.9,
    )
}
