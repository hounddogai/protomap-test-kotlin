package com.acme.transport.fleetops.defect.manifest

import com.acme.transport.kit.flags.Registry

/** One step of the defect milestone calculation. */
interface DefectMilestoneModifier {
    fun describe(): String
    fun adjust(item: DefectMilestone, value: Double): Double
}

data class TieredDefectMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDefectMilestoneAdjuster(var minimum: Double) : DefectMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDefectMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the defect milestone steps in order and keeps a trace of each. */
data class DefectMilestoneEngine(
    var steps: MutableList<DefectMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefectMilestone): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefectMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefectMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefectMilestoneEngine(flags: Registry): DefectMilestoneEngine {
    return DefectMilestoneEngine(
        steps = mutableListOf<DefectMilestoneModifier>(
            TieredDefectMilestoneAdjuster(
                mutableListOf<Double>(2322.0, 4002.0, 7863.0, 8491.0),
                mutableListOf<Double>(0.87, 1.07, 0.86, 1.2),
            ),
            MinimumDefectMilestoneAdjuster(19.9),
            FlaggedDefectMilestoneAdjuster(flags, "defect-milestone-pending", -9.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 92680.5,
    )
}
