package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the qualification schedule calculation. */
interface QualificationScheduleAdjuster {
    fun describe(): String
    fun adjust(item: QualificationSchedule, value: Double): Double
}

data class LabelQualificationScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationScheduleAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: QualificationSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingQualificationScheduleAdjuster(var step: Double) : QualificationScheduleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationSchedule, value: Double): Double = roundTo(value, step)
}

data class FlaggedQualificationScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : QualificationScheduleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: QualificationSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentQualificationScheduleAdjuster(var percent: Double) : QualificationScheduleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the qualification schedule steps in order and keeps a trace of each. */
data class QualificationScheduleEngine(
    var steps: MutableList<QualificationScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: QualificationSchedule): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationScheduleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultQualificationScheduleEngine(flags: Registry): QualificationScheduleEngine {
    return QualificationScheduleEngine(
        steps = mutableListOf<QualificationScheduleAdjuster>(
            LabelQualificationScheduleAdjuster(mutableMapOf<String, Double>("north" to 1.23)),
            RoundingQualificationScheduleAdjuster(0.05),
            FlaggedQualificationScheduleAdjuster(flags, "qualification-schedule-weighted", 6.6),
            PercentQualificationScheduleAdjuster(-15.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 40766.9,
    )
}
