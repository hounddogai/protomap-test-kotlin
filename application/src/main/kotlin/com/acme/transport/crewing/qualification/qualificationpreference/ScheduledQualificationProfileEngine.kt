package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled qualification profile calculation. */
interface ScheduledQualificationProfileStep {
    fun describe(): String
    fun adjust(item: ScheduledQualificationProfile, value: Double): Double
}

data class CapScheduledQualificationProfileAdjuster(var cap: Double) : ScheduledQualificationProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledQualificationProfile, value: Double): Double = minOf(value, cap)
}

data class ScheduledQualificationProfileBudgetOffsetAdjuster(
    var weight: Double,
) : ScheduledQualificationProfileStep {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: ScheduledQualificationProfile, value: Double): Double = value + item.budget * weight
}

data class FlaggedScheduledQualificationProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledQualificationProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledQualificationProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleScheduledQualificationProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledQualificationProfileStep {
    override fun describe(): String = "by title"

    override fun adjust(item: ScheduledQualificationProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled qualification profile steps in order and keeps a trace of each. */
data class ScheduledQualificationProfileEngine(
    var steps: MutableList<ScheduledQualificationProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledQualificationProfile): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledQualificationProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledQualificationProfileEngine(flags: Registry): ScheduledQualificationProfileEngine {
    return ScheduledQualificationProfileEngine(
        steps = mutableListOf<ScheduledQualificationProfileStep>(
            CapScheduledQualificationProfileAdjuster(16830.0),
            ScheduledQualificationProfileBudgetOffsetAdjuster(0.52),
            FlaggedScheduledQualificationProfileAdjuster(flags, "scheduled-qualification-profile-global", -9.4),
            TitleScheduledQualificationProfileAdjuster(
                mutableMapOf<String, Double>("online" to 1.0, "north" to 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 25303.3,
    )
}
