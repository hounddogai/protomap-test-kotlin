package com.acme.transport.crewing.dutyperiod.manualperiodassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped duty period assignment calculation. */
interface CappedDutyPeriodAssignmentModifier {
    fun describe(): String
    fun adjust(item: CappedDutyPeriodAssignment, value: Double): Double
}

data class TitleCappedDutyPeriodAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedDutyPeriodAssignmentModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: CappedDutyPeriodAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CappedDutyPeriodAssignmentLimitOffsetAdjuster(
    var weight: Double,
) : CappedDutyPeriodAssignmentModifier {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: CappedDutyPeriodAssignment, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

data class FlaggedCappedDutyPeriodAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedDutyPeriodAssignmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedDutyPeriodAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped duty period assignment steps in order and keeps a trace of each. */
data class CappedDutyPeriodAssignmentEngine(
    var steps: MutableList<CappedDutyPeriodAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedDutyPeriodAssignment): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedDutyPeriodAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedDutyPeriodAssignmentEngine(flags: Registry): CappedDutyPeriodAssignmentEngine {
    return CappedDutyPeriodAssignmentEngine(
        steps = mutableListOf<CappedDutyPeriodAssignmentModifier>(
            TitleCappedDutyPeriodAssignmentAdjuster(
                mutableMapOf<String, Double>("central" to 1.33, "east" to 1.33, "retail" to 1.0),
            ),
            CappedDutyPeriodAssignmentLimitOffsetAdjuster(0.92),
            FlaggedCappedDutyPeriodAssignmentAdjuster(flags, "capped-duty-period-assignment-strict", -3.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 87197.0,
    )
}
