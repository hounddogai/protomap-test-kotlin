package com.acme.transport.fleetops.defect.profile

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled defect profile calculation. */
interface ScheduledDefectProfileAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledDefectProfile, value: Double): Double
}

data class FlaggedScheduledDefectProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledDefectProfileAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledDefectProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScheduledDefectProfileAdjuster(var percent: Double) : ScheduledDefectProfileAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledDefectProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ScheduledDefectProfileRateOffsetAdjuster(var weight: Double) : ScheduledDefectProfileAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: ScheduledDefectProfile, value: Double): Double = value + item.rate * weight
}

data class CapScheduledDefectProfileAdjuster(var cap: Double) : ScheduledDefectProfileAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledDefectProfile, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled defect profile steps in order and keeps a trace of each. */
data class ScheduledDefectProfileEngine(
    var steps: MutableList<ScheduledDefectProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledDefectProfile): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledDefectProfileAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledDefectProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledDefectProfileEngine(flags: Registry): ScheduledDefectProfileEngine {
    return ScheduledDefectProfileEngine(
        steps = mutableListOf<ScheduledDefectProfileAdjuster>(
            FlaggedScheduledDefectProfileAdjuster(flags, "scheduled-defect-profile-pending", 4.8),
            PercentScheduledDefectProfileAdjuster(-20.0),
            ScheduledDefectProfileRateOffsetAdjuster(1.5),
            CapScheduledDefectProfileAdjuster(9363.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 24097.5,
    )
}
