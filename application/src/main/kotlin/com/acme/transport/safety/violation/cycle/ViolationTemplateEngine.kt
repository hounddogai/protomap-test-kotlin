package com.acme.transport.safety.violation.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the violation template calculation. */
interface ViolationTemplateAdjuster {
    fun describe(): String
    fun adjust(item: ViolationTemplate, value: Double): Double
}

data class FlaggedViolationTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ViolationTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ViolationTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ViolationTemplateCostOffsetAdjuster(var weight: Double) : ViolationTemplateAdjuster {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: ViolationTemplate, value: Double): Double = value + item.cost * weight
}

data class PercentViolationTemplateAdjuster(var percent: Double) : ViolationTemplateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredViolationTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationTemplateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingViolationTemplateAdjuster(var step: Double) : ViolationTemplateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationTemplate, value: Double): Double = roundTo(value, step)
}

data class CapViolationTemplateAdjuster(var cap: Double) : ViolationTemplateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationTemplate, value: Double): Double = minOf(value, cap)
}

/** Runs the violation template steps in order and keeps a trace of each. */
data class ViolationTemplateEngine(
    var steps: MutableList<ViolationTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ViolationTemplate): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultViolationTemplateEngine(flags: Registry): ViolationTemplateEngine {
    return ViolationTemplateEngine(
        steps = mutableListOf<ViolationTemplateAdjuster>(
            FlaggedViolationTemplateAdjuster(flags, "violation-template-lenient", -4.1),
            ViolationTemplateCostOffsetAdjuster(0.65),
            PercentViolationTemplateAdjuster(18.0),
            TieredViolationTemplateAdjuster(
                mutableListOf<Double>(3867.0, 9359.0),
                mutableListOf<Double>(0.86, 1.19),
            ),
            RoundingViolationTemplateAdjuster(0.05),
            CapViolationTemplateAdjuster(15274.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 17250.9,
    )
}
