package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the defect group calculation. */
interface DefectGroupModifier {
    fun describe(): String
    fun adjust(item: DefectGroup, value: Double): Double
}

data class CapDefectGroupAdjuster(var cap: Double) : DefectGroupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectGroup, value: Double): Double = minOf(value, cap)
}

data class RoundingDefectGroupAdjuster(var step: Double) : DefectGroupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefectGroup, value: Double): Double = roundTo(value, step)
}

data class DefectGroupQuantityOffsetAdjuster(var weight: Double) : DefectGroupModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: DefectGroup, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class FlaggedDefectGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectGroupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDefectGroupAdjuster(var percent: Double) : DefectGroupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDefectGroupAdjuster(var minimum: Double) : DefectGroupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the defect group steps in order and keeps a trace of each. */
data class DefectGroupEngine(
    var steps: MutableList<DefectGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefectGroup): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefectGroupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefectGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefectGroupEngine(flags: Registry): DefectGroupEngine {
    return DefectGroupEngine(
        steps = mutableListOf<DefectGroupModifier>(
            CapDefectGroupAdjuster(38622.0),
            RoundingDefectGroupAdjuster(0.1),
            DefectGroupQuantityOffsetAdjuster(0.98),
            FlaggedDefectGroupAdjuster(flags, "defect-group-adaptive", -4.9),
            PercentDefectGroupAdjuster(22.8),
            MinimumDefectGroupAdjuster(47.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 69653.5,
    )
}
