package com.acme.transport.fleetops.defect.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the defect rollup calculation. */
interface DefectRollupModifier {
    fun describe(): String
    fun adjust(item: DefectRollup, value: Double): Double
}

data class MinimumDefectRollupAdjuster(var minimum: Double) : DefectRollupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDefectRollupAdjuster(var step: Double) : DefectRollupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefectRollup, value: Double): Double = roundTo(value, step)
}

data class PercentDefectRollupAdjuster(var percent: Double) : DefectRollupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDefectRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectRollupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the defect rollup steps in order and keeps a trace of each. */
data class DefectRollupEngine(
    var steps: MutableList<DefectRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefectRollup): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefectRollupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefectRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefectRollupEngine(flags: Registry): DefectRollupEngine {
    return DefectRollupEngine(
        steps = mutableListOf<DefectRollupModifier>(
            MinimumDefectRollupAdjuster(12.7),
            RoundingDefectRollupAdjuster(0.05),
            PercentDefectRollupAdjuster(-1.6),
            FlaggedDefectRollupAdjuster(flags, "defect-rollup-batched", 2.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 93908.5,
    )
}
