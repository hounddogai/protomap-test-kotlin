package com.acme.transport.fleetops.defect.tier

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the defect cycle calculation. */
interface DefectCycleStep {
    fun describe(): String
    fun adjust(item: DefectCycle, value: Double): Double
}

data class CapDefectCycleAdjuster(var cap: Double) : DefectCycleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectCycle, value: Double): Double = minOf(value, cap)
}

data class DefectCycleEtaMinutesOffsetAdjuster(var weight: Double) : DefectCycleStep {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: DefectCycle, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class PercentDefectCycleAdjuster(var percent: Double) : DefectCycleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDefectCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the defect cycle steps in order and keeps a trace of each. */
data class DefectCycleEngine(
    var steps: MutableList<DefectCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefectCycle): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefectCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefectCycleEngine(): DefectCycleEngine {
    return DefectCycleEngine(
        steps = mutableListOf<DefectCycleStep>(
            CapDefectCycleAdjuster(27172.0),
            DefectCycleEtaMinutesOffsetAdjuster(1.39),
            PercentDefectCycleAdjuster(-3.1),
            TieredDefectCycleAdjuster(mutableListOf<Double>(3440.0, 5432.0), mutableListOf<Double>(0.91, 1.16)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 51709.2,
    )
}
