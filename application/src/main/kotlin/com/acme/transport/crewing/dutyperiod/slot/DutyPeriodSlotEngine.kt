package com.acme.transport.crewing.dutyperiod.slot

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the duty period slot calculation. */
interface DutyPeriodSlotStep {
    fun describe(): String
    fun adjust(item: DutyPeriodSlot, value: Double): Double
}

data class RegionDutyPeriodSlotAdjuster(var factors: MutableMap<String, Double>) : DutyPeriodSlotStep {
    override fun describe(): String = "by region"

    override fun adjust(item: DutyPeriodSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class DutyPeriodSlotWindowDaysOffsetAdjuster(var weight: Double) : DutyPeriodSlotStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: DutyPeriodSlot, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class CapDutyPeriodSlotAdjuster(var cap: Double) : DutyPeriodSlotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DutyPeriodSlot, value: Double): Double = minOf(value, cap)
}

/** Runs the duty period slot steps in order and keeps a trace of each. */
data class DutyPeriodSlotEngine(
    var steps: MutableList<DutyPeriodSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DutyPeriodSlot): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DutyPeriodSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDutyPeriodSlotEngine(): DutyPeriodSlotEngine {
    return DutyPeriodSlotEngine(
        steps = mutableListOf<DutyPeriodSlotStep>(
            RegionDutyPeriodSlotAdjuster(mutableMapOf<String, Double>("west" to 1.16, "east" to 0.94)),
            DutyPeriodSlotWindowDaysOffsetAdjuster(1.48),
            CapDutyPeriodSlotAdjuster(34015.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 66564.1,
    )
}
