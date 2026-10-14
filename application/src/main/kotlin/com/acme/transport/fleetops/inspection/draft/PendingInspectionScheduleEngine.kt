package com.acme.transport.fleetops.inspection.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending inspection schedule calculation. */
interface PendingInspectionScheduleAdjuster {
    fun describe(): String
    fun adjust(item: PendingInspectionSchedule, value: Double): Double
}

data class RoundingPendingInspectionScheduleAdjuster(var step: Double) : PendingInspectionScheduleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingInspectionSchedule, value: Double): Double = roundTo(value, step)
}

data class TieredPendingInspectionScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingInspectionScheduleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingInspectionSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotPendingInspectionScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingInspectionScheduleAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: PendingInspectionSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPendingInspectionScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingInspectionScheduleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingInspectionSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPendingInspectionScheduleAdjuster(var percent: Double) : PendingInspectionScheduleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingInspectionSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending inspection schedule steps in order and keeps a trace of each. */
data class PendingInspectionScheduleEngine(
    var steps: MutableList<PendingInspectionScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingInspectionSchedule): Double {
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

    fun evaluateAll(items: MutableList<PendingInspectionSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingInspectionScheduleEngine(flags: Registry): PendingInspectionScheduleEngine {
    return PendingInspectionScheduleEngine(
        steps = mutableListOf<PendingInspectionScheduleAdjuster>(
            RoundingPendingInspectionScheduleAdjuster(0.25),
            TieredPendingInspectionScheduleAdjuster(
                mutableListOf<Double>(7011.0, 7845.0, 9861.0),
                mutableListOf<Double>(1.11, 1.12, 1.0),
            ),
            DepotPendingInspectionScheduleAdjuster(
                mutableMapOf<String, Double>("retail" to 1.27, "east" to 0.95, "online" to 1.39),
            ),
            FlaggedPendingInspectionScheduleAdjuster(flags, "pending-inspection-schedule-primary", 6.1),
            PercentPendingInspectionScheduleAdjuster(-14.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 91397.0,
    )
}
