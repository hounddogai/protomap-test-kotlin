package com.acme.transport.crewing.dutyperiod.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the duty period snapshot calculation. */
interface DutyPeriodSnapshotModifier {
    fun describe(): String
    fun adjust(item: DutyPeriodSnapshot, value: Double): Double
}

data class PercentDutyPeriodSnapshotAdjuster(var percent: Double) : DutyPeriodSnapshotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DutyPeriodSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDutyPeriodSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DutyPeriodSnapshotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DutyPeriodSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumDutyPeriodSnapshotAdjuster(var minimum: Double) : DutyPeriodSnapshotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the duty period snapshot steps in order and keeps a trace of each. */
data class DutyPeriodSnapshotEngine(
    var steps: MutableList<DutyPeriodSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DutyPeriodSnapshot): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DutyPeriodSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDutyPeriodSnapshotEngine(flags: Registry): DutyPeriodSnapshotEngine {
    return DutyPeriodSnapshotEngine(
        steps = mutableListOf<DutyPeriodSnapshotModifier>(
            PercentDutyPeriodSnapshotAdjuster(-5.3),
            FlaggedDutyPeriodSnapshotAdjuster(flags, "duty-period-snapshot-capped", -5.1),
            MinimumDutyPeriodSnapshotAdjuster(13.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 90699.8,
    )
}
