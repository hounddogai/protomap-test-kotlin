package com.acme.transport.terminal.dwell.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient dwell milestone calculation. */
interface LenientDwellMilestoneFactor {
    fun describe(): String
    fun adjust(item: LenientDwellMilestone, value: Double): Double
}

data class PercentLenientDwellMilestoneAdjuster(var percent: Double) : LenientDwellMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientDwellMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLenientDwellMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientDwellMilestoneFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientDwellMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LenientDwellMilestoneRetriesOffsetAdjuster(var weight: Double) : LenientDwellMilestoneFactor {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: LenientDwellMilestone, value: Double): Double = value + item.retries.toDouble() * weight
}

data class FlaggedLenientDwellMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientDwellMilestoneFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientDwellMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelLenientDwellMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDwellMilestoneFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: LenientDwellMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient dwell milestone steps in order and keeps a trace of each. */
data class LenientDwellMilestoneEngine(
    var steps: MutableList<LenientDwellMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientDwellMilestone): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientDwellMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientDwellMilestoneEngine(flags: Registry): LenientDwellMilestoneEngine {
    return LenientDwellMilestoneEngine(
        steps = mutableListOf<LenientDwellMilestoneFactor>(
            PercentLenientDwellMilestoneAdjuster(-15.0),
            TieredLenientDwellMilestoneAdjuster(
                mutableListOf<Double>(1898.0, 4971.0, 7732.0, 9737.0),
                mutableListOf<Double>(1.17, 0.89, 0.9, 1.12),
            ),
            LenientDwellMilestoneRetriesOffsetAdjuster(1.46),
            FlaggedLenientDwellMilestoneAdjuster(flags, "lenient-dwell-milestone-scheduled", -0.3),
            LabelLenientDwellMilestoneAdjuster(mutableMapOf<String, Double>("north" to 1.01)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 89486.8,
    )
}
