package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the duty period template calculation. */
interface DutyPeriodTemplateStep {
    fun describe(): String
    fun adjust(item: DutyPeriodTemplate, value: Double): Double
}

data class CapDutyPeriodTemplateAdjuster(var cap: Double) : DutyPeriodTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DutyPeriodTemplate, value: Double): Double = minOf(value, cap)
}

data class MinimumDutyPeriodTemplateAdjuster(var minimum: Double) : DutyPeriodTemplateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDutyPeriodTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DutyPeriodTemplateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DutyPeriodTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelDutyPeriodTemplateAdjuster(var factors: MutableMap<String, Double>) : DutyPeriodTemplateStep {
    override fun describe(): String = "by label"

    override fun adjust(item: DutyPeriodTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the duty period template steps in order and keeps a trace of each. */
data class DutyPeriodTemplateEngine(
    var steps: MutableList<DutyPeriodTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DutyPeriodTemplate): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DutyPeriodTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDutyPeriodTemplateEngine(flags: Registry): DutyPeriodTemplateEngine {
    return DutyPeriodTemplateEngine(
        steps = mutableListOf<DutyPeriodTemplateStep>(
            CapDutyPeriodTemplateAdjuster(21879.0),
            MinimumDutyPeriodTemplateAdjuster(25.7),
            FlaggedDutyPeriodTemplateAdjuster(flags, "duty-period-template-strict", 3.4),
            LabelDutyPeriodTemplateAdjuster(mutableMapOf<String, Double>("central" to 1.35)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 6749.7,
    )
}
