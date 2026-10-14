package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped accessorial entry calculation. */
interface CappedAccessorialEntryFactor {
    fun describe(): String
    fun adjust(item: CappedAccessorialEntry, value: Double): Double
}

data class CappedAccessorialEntryGraceDaysOffsetAdjuster(var weight: Double) : CappedAccessorialEntryFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: CappedAccessorialEntry, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class CapCappedAccessorialEntryAdjuster(var cap: Double) : CappedAccessorialEntryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedAccessorialEntry, value: Double): Double = minOf(value, cap)
}

data class SourceCappedAccessorialEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedAccessorialEntryFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: CappedAccessorialEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCappedAccessorialEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedAccessorialEntryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedAccessorialEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCappedAccessorialEntryAdjuster(var step: Double) : CappedAccessorialEntryFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedAccessorialEntry, value: Double): Double = roundTo(value, step)
}

/** Runs the capped accessorial entry steps in order and keeps a trace of each. */
data class CappedAccessorialEntryEngine(
    var steps: MutableList<CappedAccessorialEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedAccessorialEntry): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedAccessorialEntryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CappedAccessorialEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedAccessorialEntryEngine(): CappedAccessorialEntryEngine {
    return CappedAccessorialEntryEngine(
        steps = mutableListOf<CappedAccessorialEntryFactor>(
            CappedAccessorialEntryGraceDaysOffsetAdjuster(0.57),
            CapCappedAccessorialEntryAdjuster(41388.0),
            SourceCappedAccessorialEntryAdjuster(mutableMapOf<String, Double>("south" to 1.23, "north" to 0.93)),
            TieredCappedAccessorialEntryAdjuster(
                mutableListOf<Double>(2170.0, 4935.0, 7154.0),
                mutableListOf<Double>(0.89, 0.82, 0.84),
            ),
            RoundingCappedAccessorialEntryAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 41289.1,
    )
}
