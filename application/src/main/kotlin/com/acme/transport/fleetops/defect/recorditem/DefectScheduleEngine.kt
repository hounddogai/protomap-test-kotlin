package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the defect schedule calculation. */
interface DefectScheduleAdjuster {
    fun describe(): String
    fun adjust(item: DefectSchedule, value: Double): Double
}

data class TieredDefectScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectScheduleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDefectScheduleAdjuster(var minimum: Double) : DefectScheduleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DefectScheduleFloorValueOffsetAdjuster(var weight: Double) : DefectScheduleAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: DefectSchedule, value: Double): Double = value + item.floorValue * weight
}

/** Runs the defect schedule steps in order and keeps a trace of each. */
data class DefectScheduleEngine(
    var steps: MutableList<DefectScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefectSchedule): Double {
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

    fun evaluateAll(items: MutableList<DefectSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefectScheduleEngine(): DefectScheduleEngine {
    return DefectScheduleEngine(
        steps = mutableListOf<DefectScheduleAdjuster>(
            TieredDefectScheduleAdjuster(
                mutableListOf<Double>(2695.0, 3620.0, 8140.0),
                mutableListOf<Double>(0.99, 0.93, 0.87),
            ),
            MinimumDefectScheduleAdjuster(17.5),
            DefectScheduleFloorValueOffsetAdjuster(0.42),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 35437.4,
    )
}
