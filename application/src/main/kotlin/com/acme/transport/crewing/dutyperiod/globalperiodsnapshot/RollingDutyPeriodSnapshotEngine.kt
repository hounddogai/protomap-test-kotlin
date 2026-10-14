package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling duty period snapshot calculation. */
interface RollingDutyPeriodSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: RollingDutyPeriodSnapshot, value: Double): Double
}

data class RoundingRollingDutyPeriodSnapshotAdjuster(var step: Double) : RollingDutyPeriodSnapshotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingDutyPeriodSnapshot, value: Double): Double = roundTo(value, step)
}

data class TieredRollingDutyPeriodSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDutyPeriodSnapshotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDutyPeriodSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RollingDutyPeriodSnapshotScoreOffsetAdjuster(
    var weight: Double,
) : RollingDutyPeriodSnapshotAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RollingDutyPeriodSnapshot, value: Double): Double = value + item.score * weight
}

data class CapRollingDutyPeriodSnapshotAdjuster(var cap: Double) : RollingDutyPeriodSnapshotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDutyPeriodSnapshot, value: Double): Double = minOf(value, cap)
}

data class MinimumRollingDutyPeriodSnapshotAdjuster(var minimum: Double) : RollingDutyPeriodSnapshotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingDutyPeriodSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRollingDutyPeriodSnapshotAdjuster(var percent: Double) : RollingDutyPeriodSnapshotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingDutyPeriodSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rolling duty period snapshot steps in order and keeps a trace of each. */
data class RollingDutyPeriodSnapshotEngine(
    var steps: MutableList<RollingDutyPeriodSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingDutyPeriodSnapshot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingDutyPeriodSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingDutyPeriodSnapshotEngine(): RollingDutyPeriodSnapshotEngine {
    return RollingDutyPeriodSnapshotEngine(
        steps = mutableListOf<RollingDutyPeriodSnapshotAdjuster>(
            RoundingRollingDutyPeriodSnapshotAdjuster(0.1),
            TieredRollingDutyPeriodSnapshotAdjuster(
                mutableListOf<Double>(837.0, 1639.0, 5878.0),
                mutableListOf<Double>(0.87, 1.02, 0.88),
            ),
            RollingDutyPeriodSnapshotScoreOffsetAdjuster(0.71),
            CapRollingDutyPeriodSnapshotAdjuster(23641.0),
            MinimumRollingDutyPeriodSnapshotAdjuster(24.2),
            PercentRollingDutyPeriodSnapshotAdjuster(-4.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 77205.5,
    )
}
