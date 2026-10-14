package com.acme.transport.crewing.dutyperiod.snapshot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the duty period bundle calculation. */
interface DutyPeriodBundleStep {
    fun describe(): String
    fun adjust(item: DutyPeriodBundle, value: Double): Double
}

data class CapDutyPeriodBundleAdjuster(var cap: Double) : DutyPeriodBundleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DutyPeriodBundle, value: Double): Double = minOf(value, cap)
}

data class RoundingDutyPeriodBundleAdjuster(var step: Double) : DutyPeriodBundleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DutyPeriodBundle, value: Double): Double = roundTo(value, step)
}

data class PercentDutyPeriodBundleAdjuster(var percent: Double) : DutyPeriodBundleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DutyPeriodBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionDutyPeriodBundleAdjuster(var factors: MutableMap<String, Double>) : DutyPeriodBundleStep {
    override fun describe(): String = "by region"

    override fun adjust(item: DutyPeriodBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDutyPeriodBundleAdjuster(var minimum: Double) : DutyPeriodBundleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the duty period bundle steps in order and keeps a trace of each. */
data class DutyPeriodBundleEngine(
    var steps: MutableList<DutyPeriodBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DutyPeriodBundle): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DutyPeriodBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDutyPeriodBundleEngine(): DutyPeriodBundleEngine {
    return DutyPeriodBundleEngine(
        steps = mutableListOf<DutyPeriodBundleStep>(
            CapDutyPeriodBundleAdjuster(46379.0),
            RoundingDutyPeriodBundleAdjuster(1.0),
            PercentDutyPeriodBundleAdjuster(13.1),
            RegionDutyPeriodBundleAdjuster(
                mutableMapOf<String, Double>("east" to 1.26, "west" to 0.76, "south" to 0.78),
            ),
            MinimumDutyPeriodBundleAdjuster(49.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 31128.8,
    )
}
