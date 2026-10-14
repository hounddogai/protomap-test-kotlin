package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default trailer exception calculation. */
interface DefaultTrailerExceptionModifier {
    fun describe(): String
    fun adjust(item: DefaultTrailerException, value: Double): Double
}

data class MinimumDefaultTrailerExceptionAdjuster(var minimum: Double) : DefaultTrailerExceptionModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultTrailerException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDefaultTrailerExceptionAdjuster(var cap: Double) : DefaultTrailerExceptionModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultTrailerException, value: Double): Double = minOf(value, cap)
}

data class LabelDefaultTrailerExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultTrailerExceptionModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: DefaultTrailerException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class DefaultTrailerExceptionCreatedAtOffsetAdjuster(
    var weight: Double,
) : DefaultTrailerExceptionModifier {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: DefaultTrailerException, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class PercentDefaultTrailerExceptionAdjuster(var percent: Double) : DefaultTrailerExceptionModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultTrailerException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDefaultTrailerExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultTrailerExceptionModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultTrailerException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default trailer exception steps in order and keeps a trace of each. */
data class DefaultTrailerExceptionEngineExtra(
    var steps: MutableList<DefaultTrailerExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultTrailerException): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultTrailerException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultTrailerExceptionEngine(flags: Registry): DefaultTrailerExceptionEngineExtra {
    return DefaultTrailerExceptionEngineExtra(
        steps = mutableListOf<DefaultTrailerExceptionModifier>(
            MinimumDefaultTrailerExceptionAdjuster(0.2),
            CapDefaultTrailerExceptionAdjuster(41506.0),
            LabelDefaultTrailerExceptionAdjuster(mutableMapOf<String, Double>("north" to 1.22)),
            DefaultTrailerExceptionCreatedAtOffsetAdjuster(0.28),
            PercentDefaultTrailerExceptionAdjuster(3.0),
            FlaggedDefaultTrailerExceptionAdjuster(flags, "default-trailer-exception-manual", 8.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 80829.7,
    )
}
