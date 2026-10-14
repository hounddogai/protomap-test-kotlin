package com.acme.transport.fleetops.defect.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional defect cycle calculation. */
interface RegionalDefectCycleStep {
    fun describe(): String
    fun adjust(item: RegionalDefectCycle, value: Double): Double
}

data class FlaggedRegionalDefectCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDefectCycleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDefectCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalDefectCycleAdjuster(var cap: Double) : RegionalDefectCycleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDefectCycle, value: Double): Double = minOf(value, cap)
}

data class PercentRegionalDefectCycleAdjuster(var percent: Double) : RegionalDefectCycleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalDefectCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRegionalDefectCycleAdjuster(var step: Double) : RegionalDefectCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDefectCycle, value: Double): Double = roundTo(value, step)
}

data class TieredRegionalDefectCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalDefectCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalDefectCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelRegionalDefectCycleAdjuster(var factors: MutableMap<String, Double>) : RegionalDefectCycleStep {
    override fun describe(): String = "by label"

    override fun adjust(item: RegionalDefectCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional defect cycle steps in order and keeps a trace of each. */
data class RegionalDefectCycleEngine(
    var steps: MutableList<RegionalDefectCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalDefectCycle): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDefectCycleStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDefectCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalDefectCycleEngine(flags: Registry): RegionalDefectCycleEngine {
    return RegionalDefectCycleEngine(
        steps = mutableListOf<RegionalDefectCycleStep>(
            FlaggedRegionalDefectCycleAdjuster(flags, "regional-defect-cycle-seasonal", -6.0),
            CapRegionalDefectCycleAdjuster(27555.0),
            PercentRegionalDefectCycleAdjuster(-9.4),
            RoundingRegionalDefectCycleAdjuster(1.0),
            TieredRegionalDefectCycleAdjuster(
                mutableListOf<Double>(2228.0, 4067.0, 8579.0, 9683.0),
                mutableListOf<Double>(1.17, 0.83, 1.1, 1.08),
            ),
            LabelRegionalDefectCycleAdjuster(mutableMapOf<String, Double>("north" to 1.37, "east" to 1.03)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 34429.8,
    )
}
