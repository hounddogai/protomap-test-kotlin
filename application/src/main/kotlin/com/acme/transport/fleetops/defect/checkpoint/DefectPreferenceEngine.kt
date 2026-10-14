package com.acme.transport.fleetops.defect.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the defect preference calculation. */
interface DefectPreferenceStep {
    fun describe(): String
    fun adjust(item: DefectPreference, value: Double): Double
}

data class DepotDefectPreferenceAdjuster(var factors: MutableMap<String, Double>) : DefectPreferenceStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: DefectPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDefectPreferenceAdjuster(var cap: Double) : DefectPreferenceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectPreference, value: Double): Double = minOf(value, cap)
}

data class TieredDefectPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectPreferenceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDefectPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectPreferenceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDefectPreferenceAdjuster(var step: Double) : DefectPreferenceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefectPreference, value: Double): Double = roundTo(value, step)
}

data class PercentDefectPreferenceAdjuster(var percent: Double) : DefectPreferenceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the defect preference steps in order and keeps a trace of each. */
data class DefectPreferenceEngine(
    var steps: MutableList<DefectPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefectPreference): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefectPreferenceStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefectPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefectPreferenceEngine(flags: Registry): DefectPreferenceEngine {
    return DefectPreferenceEngine(
        steps = mutableListOf<DefectPreferenceStep>(
            DepotDefectPreferenceAdjuster(mutableMapOf<String, Double>("north" to 1.11, "south" to 1.24)),
            CapDefectPreferenceAdjuster(30650.0),
            TieredDefectPreferenceAdjuster(
                mutableListOf<Double>(249.0, 6404.0),
                mutableListOf<Double>(0.96, 1.0),
            ),
            FlaggedDefectPreferenceAdjuster(flags, "defect-preference-weighted", -5.7),
            RoundingDefectPreferenceAdjuster(0.1),
            PercentDefectPreferenceAdjuster(-3.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 42621.9,
    )
}
