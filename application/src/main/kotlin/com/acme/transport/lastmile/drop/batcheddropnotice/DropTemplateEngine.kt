package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the drop template calculation. */
interface DropTemplateModifier {
    fun describe(): String
    fun adjust(item: DropTemplate, value: Double): Double
}

data class FlaggedDropTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DropTemplateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DropTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DropTemplateMaximumOffsetAdjuster(var weight: Double) : DropTemplateModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: DropTemplate, value: Double): Double = value + item.maximum * weight
}

data class MinimumDropTemplateAdjuster(var minimum: Double) : DropTemplateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DropTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDropTemplateAdjuster(var percent: Double) : DropTemplateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DropTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the drop template steps in order and keeps a trace of each. */
data class DropTemplateEngine(
    var steps: MutableList<DropTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DropTemplate): Double {
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

    fun evaluateAll(items: MutableList<DropTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDropTemplateEngine(flags: Registry): DropTemplateEngine {
    return DropTemplateEngine(
        steps = mutableListOf<DropTemplateModifier>(
            FlaggedDropTemplateAdjuster(flags, "drop-template-primary", -6.6),
            DropTemplateMaximumOffsetAdjuster(1.44),
            MinimumDropTemplateAdjuster(45.3),
            PercentDropTemplateAdjuster(12.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 12987.7,
    )
}
