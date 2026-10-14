package com.acme.transport.terminal.dwell.dwelltemplate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling dwell template calculation. */
interface RollingDwellTemplateFactor {
    fun describe(): String
    fun adjust(item: RollingDwellTemplate, value: Double): Double
}

data class CapRollingDwellTemplateAdjuster(var cap: Double) : RollingDwellTemplateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDwellTemplate, value: Double): Double = minOf(value, cap)
}

data class FlaggedRollingDwellTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingDwellTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingDwellTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRollingDwellTemplateAdjuster(var percent: Double) : RollingDwellTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingDwellTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rolling dwell template steps in order and keeps a trace of each. */
data class RollingDwellTemplateEngine(
    var steps: MutableList<RollingDwellTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingDwellTemplate): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingDwellTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingDwellTemplateEngine(flags: Registry): RollingDwellTemplateEngine {
    return RollingDwellTemplateEngine(
        steps = mutableListOf<RollingDwellTemplateFactor>(
            CapRollingDwellTemplateAdjuster(1217.0),
            FlaggedRollingDwellTemplateAdjuster(flags, "rolling-dwell-template-seasonal", 7.0),
            PercentRollingDwellTemplateAdjuster(-15.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 80029.0,
    )
}
