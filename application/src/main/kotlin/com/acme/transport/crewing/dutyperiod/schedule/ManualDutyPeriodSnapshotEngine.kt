package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual duty period snapshot calculation. */
interface ManualDutyPeriodSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: ManualDutyPeriodSnapshot, value: Double): Double
}

data class CapManualDutyPeriodSnapshotAdjuster(var cap: Double) : ManualDutyPeriodSnapshotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualDutyPeriodSnapshot, value: Double): Double = minOf(value, cap)
}

data class FlaggedManualDutyPeriodSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualDutyPeriodSnapshotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualDutyPeriodSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SegmentManualDutyPeriodSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualDutyPeriodSnapshotAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: ManualDutyPeriodSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumManualDutyPeriodSnapshotAdjuster(var minimum: Double) : ManualDutyPeriodSnapshotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualDutyPeriodSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual duty period snapshot steps in order and keeps a trace of each. */
data class ManualDutyPeriodSnapshotEngine(
    var steps: MutableList<ManualDutyPeriodSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualDutyPeriodSnapshot): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualDutyPeriodSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualDutyPeriodSnapshotEngine(flags: Registry): ManualDutyPeriodSnapshotEngine {
    return ManualDutyPeriodSnapshotEngine(
        steps = mutableListOf<ManualDutyPeriodSnapshotAdjuster>(
            CapManualDutyPeriodSnapshotAdjuster(33734.0),
            FlaggedManualDutyPeriodSnapshotAdjuster(flags, "manual-duty-period-snapshot-strict", 6.8),
            SegmentManualDutyPeriodSnapshotAdjuster(mutableMapOf<String, Double>("central" to 1.33)),
            MinimumManualDutyPeriodSnapshotAdjuster(32.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 38306.2,
    )
}
