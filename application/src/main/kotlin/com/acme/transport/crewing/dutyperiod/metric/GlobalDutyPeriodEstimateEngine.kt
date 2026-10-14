package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.kit.flags.Registry

/** One step of the global duty period estimate calculation. */
interface GlobalDutyPeriodEstimateFactor {
    fun describe(): String
    fun adjust(item: GlobalDutyPeriodEstimate, value: Double): Double
}

data class CapGlobalDutyPeriodEstimateAdjuster(var cap: Double) : GlobalDutyPeriodEstimateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalDutyPeriodEstimate, value: Double): Double = minOf(value, cap)
}

data class FlaggedGlobalDutyPeriodEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalDutyPeriodEstimateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalDutyPeriodEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class GlobalDutyPeriodEstimateMaximumOffsetAdjuster(var weight: Double) : GlobalDutyPeriodEstimateFactor {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: GlobalDutyPeriodEstimate, value: Double): Double = value + item.maximum * weight
}

data class MinimumGlobalDutyPeriodEstimateAdjuster(var minimum: Double) : GlobalDutyPeriodEstimateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalDutyPeriodEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentGlobalDutyPeriodEstimateAdjuster(var percent: Double) : GlobalDutyPeriodEstimateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalDutyPeriodEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryGlobalDutyPeriodEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalDutyPeriodEstimateFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: GlobalDutyPeriodEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the global duty period estimate steps in order and keeps a trace of each. */
data class GlobalDutyPeriodEstimateEngine(
    var steps: MutableList<GlobalDutyPeriodEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalDutyPeriodEstimate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalDutyPeriodEstimateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalDutyPeriodEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalDutyPeriodEstimateEngine(flags: Registry): GlobalDutyPeriodEstimateEngine {
    return GlobalDutyPeriodEstimateEngine(
        steps = mutableListOf<GlobalDutyPeriodEstimateFactor>(
            CapGlobalDutyPeriodEstimateAdjuster(14685.0),
            FlaggedGlobalDutyPeriodEstimateAdjuster(flags, "global-duty-period-estimate-fallback", -3.0),
            GlobalDutyPeriodEstimateMaximumOffsetAdjuster(1.86),
            MinimumGlobalDutyPeriodEstimateAdjuster(17.0),
            PercentGlobalDutyPeriodEstimateAdjuster(15.2),
            CountryGlobalDutyPeriodEstimateAdjuster(mutableMapOf<String, Double>("west" to 0.99)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 84788.8,
    )
}
