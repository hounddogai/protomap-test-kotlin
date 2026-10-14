package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rest break variance calculation. */
interface RestBreakVarianceModifier {
    fun describe(): String
    fun adjust(item: RestBreakVariance, value: Double): Double
}

data class LaneRestBreakVarianceAdjuster(var factors: MutableMap<String, Double>) : RestBreakVarianceModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: RestBreakVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRestBreakVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRestBreakVarianceAdjuster(var step: Double) : RestBreakVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakVariance, value: Double): Double = roundTo(value, step)
}

data class RestBreakVarianceEndsAtOffsetAdjuster(var weight: Double) : RestBreakVarianceModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: RestBreakVariance, value: Double): Double = value + item.endsAt.toDouble() * weight
}

/** Runs the rest break variance steps in order and keeps a trace of each. */
data class RestBreakVarianceEngine(
    var steps: MutableList<RestBreakVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RestBreakVariance): Double {
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

    fun evaluateAll(items: MutableList<RestBreakVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRestBreakVarianceEngine(flags: Registry): RestBreakVarianceEngine {
    return RestBreakVarianceEngine(
        steps = mutableListOf<RestBreakVarianceModifier>(
            LaneRestBreakVarianceAdjuster(
                mutableMapOf<String, Double>("west" to 1.16, "online" to 1.04, "north" to 1.21),
            ),
            FlaggedRestBreakVarianceAdjuster(flags, "rest-break-variance-scheduled", -7.4),
            RoundingRestBreakVarianceAdjuster(0.5),
            RestBreakVarianceEndsAtOffsetAdjuster(1.92),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 63614.1,
    )
}
