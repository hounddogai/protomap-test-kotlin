package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer exception calculation. */
interface TrailerExceptionModifier {
    fun describe(): String
    fun adjust(item: TrailerException, value: Double): Double
}

data class FlaggedTrailerExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerExceptionModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TrailerExceptionMinimumOffsetAdjuster(var weight: Double) : TrailerExceptionModifier {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: TrailerException, value: Double): Double = value + item.minimum * weight
}

data class RoundingTrailerExceptionAdjuster(var step: Double) : TrailerExceptionModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerException, value: Double): Double = roundTo(value, step)
}

data class MinimumTrailerExceptionAdjuster(var minimum: Double) : TrailerExceptionModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelTrailerExceptionAdjuster(var factors: MutableMap<String, Double>) : TrailerExceptionModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: TrailerException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTrailerExceptionAdjuster(var cap: Double) : TrailerExceptionModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerException, value: Double): Double = minOf(value, cap)
}

/** Runs the trailer exception steps in order and keeps a trace of each. */
data class TrailerExceptionEngine(
    var steps: MutableList<TrailerExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerException): Double {
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

    fun evaluateAll(items: MutableList<TrailerException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerExceptionEngine(flags: Registry): TrailerExceptionEngine {
    return TrailerExceptionEngine(
        steps = mutableListOf<TrailerExceptionModifier>(
            FlaggedTrailerExceptionAdjuster(flags, "trailer-exception-lenient", 9.2),
            TrailerExceptionMinimumOffsetAdjuster(0.26),
            RoundingTrailerExceptionAdjuster(0.05),
            MinimumTrailerExceptionAdjuster(16.6),
            LabelTrailerExceptionAdjuster(
                mutableMapOf<String, Double>("retail" to 1.2, "central" to 0.88, "east" to 1.3),
            ),
            CapTrailerExceptionAdjuster(19049.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 70040.1,
    )
}
