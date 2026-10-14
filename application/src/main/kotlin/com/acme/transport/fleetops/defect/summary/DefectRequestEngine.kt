package com.acme.transport.fleetops.defect.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the defect request calculation. */
interface DefectRequestAdjuster {
    fun describe(): String
    fun adjust(item: DefectRequest, value: Double): Double
}

data class DefectRequestFuelLitersOffsetAdjuster(var weight: Double) : DefectRequestAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: DefectRequest, value: Double): Double = value + item.fuelLiters * weight
}

data class FlaggedDefectRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectRequestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDefectRequestAdjuster(var step: Double) : DefectRequestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefectRequest, value: Double): Double = roundTo(value, step)
}

data class MinimumDefectRequestAdjuster(var minimum: Double) : DefectRequestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDefectRequestAdjuster(var percent: Double) : DefectRequestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDefectRequestAdjuster(var cap: Double) : DefectRequestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectRequest, value: Double): Double = minOf(value, cap)
}

/** Runs the defect request steps in order and keeps a trace of each. */
data class DefectRequestEngine(
    var steps: MutableList<DefectRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefectRequest): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefectRequestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefectRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefectRequestEngine(flags: Registry): DefectRequestEngine {
    return DefectRequestEngine(
        steps = mutableListOf<DefectRequestAdjuster>(
            DefectRequestFuelLitersOffsetAdjuster(0.07),
            FlaggedDefectRequestAdjuster(flags, "defect-request-strict", -7.6),
            RoundingDefectRequestAdjuster(0.25),
            MinimumDefectRequestAdjuster(17.3),
            PercentDefectRequestAdjuster(21.4),
            CapDefectRequestAdjuster(47171.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 45604.3,
    )
}
