package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.flags.Registry

/** One step of the lenient near miss batch calculation. */
interface LenientNearMissBatchStep {
    fun describe(): String
    fun adjust(item: LenientNearMissBatch, value: Double): Double
}

data class TitleLenientNearMissBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientNearMissBatchStep {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientNearMissBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLenientNearMissBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientNearMissBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientNearMissBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LenientNearMissBatchFuelLitersOffsetAdjuster(var weight: Double) : LenientNearMissBatchStep {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: LenientNearMissBatch, value: Double): Double = value + item.fuelLiters * weight
}

data class TieredLenientNearMissBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientNearMissBatchStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientNearMissBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLenientNearMissBatchAdjuster(var percent: Double) : LenientNearMissBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientNearMissBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLenientNearMissBatchAdjuster(var minimum: Double) : LenientNearMissBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientNearMissBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient near miss batch steps in order and keeps a trace of each. */
data class LenientNearMissBatchEngine(
    var steps: MutableList<LenientNearMissBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientNearMissBatch): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientNearMissBatchStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientNearMissBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientNearMissBatchEngine(flags: Registry): LenientNearMissBatchEngine {
    return LenientNearMissBatchEngine(
        steps = mutableListOf<LenientNearMissBatchStep>(
            TitleLenientNearMissBatchAdjuster(mutableMapOf<String, Double>("west" to 1.36)),
            FlaggedLenientNearMissBatchAdjuster(flags, "lenient-near-miss-batch-global", -0.7),
            LenientNearMissBatchFuelLitersOffsetAdjuster(1.83),
            TieredLenientNearMissBatchAdjuster(
                mutableListOf<Double>(3054.0, 7636.0, 8222.0, 9546.0),
                mutableListOf<Double>(1.08, 0.89, 0.85, 1.05),
            ),
            PercentLenientNearMissBatchAdjuster(12.6),
            MinimumLenientNearMissBatchAdjuster(47.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 55889.7,
    )
}
