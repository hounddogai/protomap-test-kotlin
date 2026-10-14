package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling redelivery template calculation. */
interface RollingRedeliveryTemplateAdjuster {
    fun describe(): String
    fun adjust(item: RollingRedeliveryTemplate, value: Double): Double
}

data class FlaggedRollingRedeliveryTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingRedeliveryTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingRedeliveryTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRollingRedeliveryTemplateAdjuster(var cap: Double) : RollingRedeliveryTemplateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingRedeliveryTemplate, value: Double): Double = minOf(value, cap)
}

data class PercentRollingRedeliveryTemplateAdjuster(var percent: Double) : RollingRedeliveryTemplateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingRedeliveryTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRollingRedeliveryTemplateAdjuster(var minimum: Double) : RollingRedeliveryTemplateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingRedeliveryTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling redelivery template steps in order and keeps a trace of each. */
data class RollingRedeliveryTemplateEngine(
    var steps: MutableList<RollingRedeliveryTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingRedeliveryTemplate): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingRedeliveryTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingRedeliveryTemplateEngine(flags: Registry): RollingRedeliveryTemplateEngine {
    return RollingRedeliveryTemplateEngine(
        steps = mutableListOf<RollingRedeliveryTemplateAdjuster>(
            FlaggedRollingRedeliveryTemplateAdjuster(flags, "rolling-redelivery-template-global", -3.8),
            CapRollingRedeliveryTemplateAdjuster(15501.0),
            PercentRollingRedeliveryTemplateAdjuster(23.9),
            MinimumRollingRedeliveryTemplateAdjuster(18.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 44739.6,
    )
}
