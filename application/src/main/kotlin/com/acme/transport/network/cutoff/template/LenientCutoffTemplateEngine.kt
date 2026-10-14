package com.acme.transport.network.cutoff.template

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient cutoff template calculation. */
interface LenientCutoffTemplateStep {
    fun describe(): String
    fun adjust(item: LenientCutoffTemplate, value: Double): Double
}

data class CapLenientCutoffTemplateAdjuster(var cap: Double) : LenientCutoffTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientCutoffTemplate, value: Double): Double = minOf(value, cap)
}

data class PercentLenientCutoffTemplateAdjuster(var percent: Double) : LenientCutoffTemplateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientCutoffTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLenientCutoffTemplateAdjuster(var step: Double) : LenientCutoffTemplateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientCutoffTemplate, value: Double): Double = roundTo(value, step)
}

data class ChannelLenientCutoffTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientCutoffTemplateStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: LenientCutoffTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LenientCutoffTemplateQuantityOffsetAdjuster(var weight: Double) : LenientCutoffTemplateStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: LenientCutoffTemplate, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

/** Runs the lenient cutoff template steps in order and keeps a trace of each. */
data class LenientCutoffTemplateEngine(
    var steps: MutableList<LenientCutoffTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientCutoffTemplate): Double {
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

    fun evaluateAll(items: MutableList<LenientCutoffTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientCutoffTemplateEngine(): LenientCutoffTemplateEngine {
    return LenientCutoffTemplateEngine(
        steps = mutableListOf<LenientCutoffTemplateStep>(
            CapLenientCutoffTemplateAdjuster(31077.0),
            PercentLenientCutoffTemplateAdjuster(23.6),
            RoundingLenientCutoffTemplateAdjuster(0.05),
            ChannelLenientCutoffTemplateAdjuster(mutableMapOf<String, Double>("west" to 1.13, "retail" to 0.79)),
            LenientCutoffTemplateQuantityOffsetAdjuster(1.45),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 81764.1,
    )
}
