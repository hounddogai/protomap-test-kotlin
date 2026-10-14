package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy trailer yard exception calculation. */
interface LegacyTrailerYardExceptionStep {
    fun describe(): String
    fun adjust(item: LegacyTrailerYardException, value: Double): Double
}

data class MinimumLegacyTrailerYardExceptionAdjuster(var minimum: Double) : LegacyTrailerYardExceptionStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyTrailerYardException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SegmentLegacyTrailerYardExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyTrailerYardExceptionStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: LegacyTrailerYardException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLegacyTrailerYardExceptionAdjuster(var percent: Double) : LegacyTrailerYardExceptionStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyTrailerYardException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLegacyTrailerYardExceptionAdjuster(var step: Double) : LegacyTrailerYardExceptionStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyTrailerYardException, value: Double): Double = roundTo(value, step)
}

data class CapLegacyTrailerYardExceptionAdjuster(var cap: Double) : LegacyTrailerYardExceptionStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyTrailerYardException, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy trailer yard exception steps in order and keeps a trace of each. */
data class LegacyTrailerYardExceptionEngine(
    var steps: MutableList<LegacyTrailerYardExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyTrailerYardException): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyTrailerYardExceptionStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyTrailerYardException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyTrailerYardExceptionEngine(): LegacyTrailerYardExceptionEngine {
    return LegacyTrailerYardExceptionEngine(
        steps = mutableListOf<LegacyTrailerYardExceptionStep>(
            MinimumLegacyTrailerYardExceptionAdjuster(13.3),
            SegmentLegacyTrailerYardExceptionAdjuster(
                mutableMapOf<String, Double>("west" to 0.72, "online" to 1.15, "east" to 1.17),
            ),
            PercentLegacyTrailerYardExceptionAdjuster(-18.2),
            RoundingLegacyTrailerYardExceptionAdjuster(5.0),
            CapLegacyTrailerYardExceptionAdjuster(24433.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 18165.7,
    )
}
