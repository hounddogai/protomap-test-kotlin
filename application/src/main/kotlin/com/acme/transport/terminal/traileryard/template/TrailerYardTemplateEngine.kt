package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer yard template calculation. */
interface TrailerYardTemplateModifier {
    fun describe(): String
    fun adjust(item: TrailerYardTemplate, value: Double): Double
}

data class PercentTrailerYardTemplateAdjuster(var percent: Double) : TrailerYardTemplateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerYardTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTrailerYardTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerYardTemplateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerYardTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTrailerYardTemplateAdjuster(var cap: Double) : TrailerYardTemplateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerYardTemplate, value: Double): Double = minOf(value, cap)
}

data class LaneTrailerYardTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : TrailerYardTemplateModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: TrailerYardTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTrailerYardTemplateAdjuster(var minimum: Double) : TrailerYardTemplateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerYardTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the trailer yard template steps in order and keeps a trace of each. */
data class TrailerYardTemplateEngine(
    var steps: MutableList<TrailerYardTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerYardTemplate): Double {
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

    fun evaluateAll(items: MutableList<TrailerYardTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerYardTemplateEngine(flags: Registry): TrailerYardTemplateEngine {
    return TrailerYardTemplateEngine(
        steps = mutableListOf<TrailerYardTemplateModifier>(
            PercentTrailerYardTemplateAdjuster(-12.9),
            FlaggedTrailerYardTemplateAdjuster(flags, "trailer-yard-template-seasonal", -8.5),
            CapTrailerYardTemplateAdjuster(16587.0),
            LaneTrailerYardTemplateAdjuster(
                mutableMapOf<String, Double>("south" to 0.87, "retail" to 1.25, "west" to 0.89),
            ),
            MinimumTrailerYardTemplateAdjuster(36.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 68518.7,
    )
}
