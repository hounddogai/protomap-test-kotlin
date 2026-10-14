package com.acme.transport.freightpay.accessorial.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the accessorial baseline calculation. */
interface AccessorialBaselineAdjuster {
    fun describe(): String
    fun adjust(item: AccessorialBaseline, value: Double): Double
}

data class FlaggedAccessorialBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AccessorialBaselineAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AccessorialBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAccessorialBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AccessorialBaselineAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AccessorialBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAccessorialBaselineAdjuster(var percent: Double) : AccessorialBaselineAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AccessorialBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryAccessorialBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : AccessorialBaselineAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: AccessorialBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAccessorialBaselineAdjuster(var minimum: Double) : AccessorialBaselineAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the accessorial baseline steps in order and keeps a trace of each. */
data class AccessorialBaselineEngine(
    var steps: MutableList<AccessorialBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AccessorialBaseline): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAccessorialBaselineEngine(flags: Registry): AccessorialBaselineEngine {
    return AccessorialBaselineEngine(
        steps = mutableListOf<AccessorialBaselineAdjuster>(
            FlaggedAccessorialBaselineAdjuster(flags, "accessorial-baseline-capped", 5.5),
            TieredAccessorialBaselineAdjuster(
                mutableListOf<Double>(15.0, 755.0, 1014.0, 1112.0),
                mutableListOf<Double>(1.12, 0.87, 0.9, 0.89),
            ),
            PercentAccessorialBaselineAdjuster(7.1),
            CountryAccessorialBaselineAdjuster(mutableMapOf<String, Double>("east" to 1.31, "central" to 0.9)),
            MinimumAccessorialBaselineAdjuster(14.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 80511.2,
    )
}
