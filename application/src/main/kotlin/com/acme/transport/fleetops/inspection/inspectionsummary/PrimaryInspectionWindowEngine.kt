package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.kit.flags.Registry

/** One step of the primary inspection window calculation. */
interface PrimaryInspectionWindowAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryInspectionWindow, value: Double): Double
}

data class CapPrimaryInspectionWindowAdjuster(var cap: Double) : PrimaryInspectionWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryInspectionWindow, value: Double): Double = minOf(value, cap)
}

data class FlaggedPrimaryInspectionWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryInspectionWindowAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryInspectionWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PrimaryInspectionWindowCapacityOffsetAdjuster(var weight: Double) : PrimaryInspectionWindowAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: PrimaryInspectionWindow, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class PercentPrimaryInspectionWindowAdjuster(var percent: Double) : PrimaryInspectionWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryInspectionWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the primary inspection window steps in order and keeps a trace of each. */
data class PrimaryInspectionWindowEngine(
    var steps: MutableList<PrimaryInspectionWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryInspectionWindow): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryInspectionWindowAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryInspectionWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryInspectionWindowEngine(flags: Registry): PrimaryInspectionWindowEngine {
    return PrimaryInspectionWindowEngine(
        steps = mutableListOf<PrimaryInspectionWindowAdjuster>(
            CapPrimaryInspectionWindowAdjuster(34732.0),
            FlaggedPrimaryInspectionWindowAdjuster(flags, "primary-inspection-window-pending", -5.7),
            PrimaryInspectionWindowCapacityOffsetAdjuster(1.68),
            PercentPrimaryInspectionWindowAdjuster(-17.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 90190.5,
    )
}
