package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered trailer yard exception calculation. */
interface TieredTrailerYardExceptionAdjuster {
    fun describe(): String
    fun adjust(item: TieredTrailerYardException, value: Double): Double
}

data class MinimumTieredTrailerYardExceptionAdjuster(var minimum: Double) : TieredTrailerYardExceptionAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredTrailerYardException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingTieredTrailerYardExceptionAdjuster(var step: Double) : TieredTrailerYardExceptionAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredTrailerYardException, value: Double): Double = roundTo(value, step)
}

data class PercentTieredTrailerYardExceptionAdjuster(var percent: Double) : TieredTrailerYardExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredTrailerYardException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTieredTrailerYardExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredTrailerYardExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredTrailerYardException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DepotTieredTrailerYardExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredTrailerYardExceptionAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: TieredTrailerYardException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered trailer yard exception steps in order and keeps a trace of each. */
data class TieredTrailerYardExceptionEngine(
    var steps: MutableList<TieredTrailerYardExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredTrailerYardException): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredTrailerYardException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredTrailerYardExceptionEngine(flags: Registry): TieredTrailerYardExceptionEngine {
    return TieredTrailerYardExceptionEngine(
        steps = mutableListOf<TieredTrailerYardExceptionAdjuster>(
            MinimumTieredTrailerYardExceptionAdjuster(3.4),
            RoundingTieredTrailerYardExceptionAdjuster(5.0),
            PercentTieredTrailerYardExceptionAdjuster(-2.2),
            FlaggedTieredTrailerYardExceptionAdjuster(flags, "tiered-trailer-yard-exception-weighted", -4.9),
            DepotTieredTrailerYardExceptionAdjuster(
                mutableMapOf<String, Double>("online" to 1.28, "central" to 0.84, "north" to 1.36),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 6320.9,
    )
}
