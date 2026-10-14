package com.acme.transport.customs.declaration.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the declaration notice calculation. */
interface DeclarationNoticeModifier {
    fun describe(): String
    fun adjust(item: DeclarationNotice, value: Double): Double
}

data class CapDeclarationNoticeAdjuster(var cap: Double) : DeclarationNoticeModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationNotice, value: Double): Double = minOf(value, cap)
}

data class RoundingDeclarationNoticeAdjuster(var step: Double) : DeclarationNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationNotice, value: Double): Double = roundTo(value, step)
}

data class TieredDeclarationNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationNoticeModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDeclarationNoticeAdjuster(var minimum: Double) : DeclarationNoticeModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DeclarationNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDeclarationNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DeclarationNoticeWindowDaysOffsetAdjuster(var weight: Double) : DeclarationNoticeModifier {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: DeclarationNotice, value: Double): Double = value + item.windowDays.toDouble() * weight
}

/** Runs the declaration notice steps in order and keeps a trace of each. */
data class DeclarationNoticeEngine(
    var steps: MutableList<DeclarationNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DeclarationNotice): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationNoticeModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DeclarationNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDeclarationNoticeEngine(flags: Registry): DeclarationNoticeEngine {
    return DeclarationNoticeEngine(
        steps = mutableListOf<DeclarationNoticeModifier>(
            CapDeclarationNoticeAdjuster(29323.0),
            RoundingDeclarationNoticeAdjuster(1.0),
            TieredDeclarationNoticeAdjuster(
                mutableListOf<Double>(2744.0, 8931.0),
                mutableListOf<Double>(0.94, 0.87),
            ),
            MinimumDeclarationNoticeAdjuster(12.4),
            FlaggedDeclarationNoticeAdjuster(flags, "declaration-notice-strict", -7.4),
            DeclarationNoticeWindowDaysOffsetAdjuster(1.43),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 29823.4,
    )
}
