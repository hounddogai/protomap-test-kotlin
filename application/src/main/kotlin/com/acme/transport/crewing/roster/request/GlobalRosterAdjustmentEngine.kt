package com.acme.transport.crewing.roster.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global roster adjustment calculation. */
interface GlobalRosterAdjustmentFactor {
    fun describe(): String
    fun adjust(item: GlobalRosterAdjustment, value: Double): Double
}

data class PercentGlobalRosterAdjustmentAdjuster(var percent: Double) : GlobalRosterAdjustmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalRosterAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredGlobalRosterAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalRosterAdjustmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalRosterAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalRosterAdjustmentAdjuster(var cap: Double) : GlobalRosterAdjustmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalRosterAdjustment, value: Double): Double = minOf(value, cap)
}

data class FlaggedGlobalRosterAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalRosterAdjustmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalRosterAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleGlobalRosterAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalRosterAdjustmentFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: GlobalRosterAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the global roster adjustment steps in order and keeps a trace of each. */
data class GlobalRosterAdjustmentEngine(
    var steps: MutableList<GlobalRosterAdjustmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalRosterAdjustment): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalRosterAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalRosterAdjustmentEngine(flags: Registry): GlobalRosterAdjustmentEngine {
    return GlobalRosterAdjustmentEngine(
        steps = mutableListOf<GlobalRosterAdjustmentFactor>(
            PercentGlobalRosterAdjustmentAdjuster(11.4),
            TieredGlobalRosterAdjustmentAdjuster(
                mutableListOf<Double>(2967.0, 7961.0, 8422.0),
                mutableListOf<Double>(0.81, 1.18, 1.14),
            ),
            CapGlobalRosterAdjustmentAdjuster(19141.0),
            FlaggedGlobalRosterAdjustmentAdjuster(flags, "global-roster-adjustment-weighted", 0.0),
            TitleGlobalRosterAdjustmentAdjuster(mutableMapOf<String, Double>("retail" to 0.94)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 76490.0,
    )
}
