package com.acme.transport.terminal.dwell.preference

import com.acme.transport.kit.flags.Registry

/** One step of the fallback dwell exception calculation. */
interface FallbackDwellExceptionFactor {
    fun describe(): String
    fun adjust(item: FallbackDwellException, value: Double): Double
}

data class CapFallbackDwellExceptionAdjuster(var cap: Double) : FallbackDwellExceptionFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDwellException, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackDwellExceptionAdjuster(var percent: Double) : FallbackDwellExceptionFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackDwellException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredFallbackDwellExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackDwellExceptionFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackDwellException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameFallbackDwellExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackDwellExceptionFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: FallbackDwellException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FallbackDwellExceptionStartsAtOffsetAdjuster(var weight: Double) : FallbackDwellExceptionFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: FallbackDwellException, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class FlaggedFallbackDwellExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackDwellExceptionFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackDwellException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback dwell exception steps in order and keeps a trace of each. */
data class FallbackDwellExceptionEngine(
    var steps: MutableList<FallbackDwellExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackDwellException): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackDwellExceptionFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackDwellException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackDwellExceptionEngine(flags: Registry): FallbackDwellExceptionEngine {
    return FallbackDwellExceptionEngine(
        steps = mutableListOf<FallbackDwellExceptionFactor>(
            CapFallbackDwellExceptionAdjuster(11952.0),
            PercentFallbackDwellExceptionAdjuster(-24.2),
            TieredFallbackDwellExceptionAdjuster(
                mutableListOf<Double>(894.0, 6467.0, 8203.0, 9536.0),
                mutableListOf<Double>(0.92, 1.0, 0.91, 0.83),
            ),
            NameFallbackDwellExceptionAdjuster(
                mutableMapOf<String, Double>("retail" to 1.18, "central" to 0.91, "south" to 1.13),
            ),
            FallbackDwellExceptionStartsAtOffsetAdjuster(1.55),
            FlaggedFallbackDwellExceptionAdjuster(flags, "fallback-dwell-exception-rolling", -0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 5093.6,
    )
}
